use tokio::sync::broadcast;
use crate::{AppState, VoteRequest};

pub struct VoteProcessor {
    tx: Option<broadcast::Sender<VoteRequest>>,
}

impl VoteProcessor {
    /// Create a new empty processor (placeholder)
    pub fn new_empty() -> Self {
        VoteProcessor { tx: None }
    }

    /// Create a new vote processor with async workers
    pub fn new(state: AppState, num_workers: usize) -> Self {
        let (tx, _rx) = broadcast::channel::<VoteRequest>(1_000_000); // 1M buffer like Go
        
        let workers = if num_workers == 0 {
            (num_cpus::get() * 32).min(4096).max(128)
        } else {
            num_workers
        };

        println!("🔄 Vote processor starting with {} workers", workers);

        // Spawn worker tasks
        for _ in 0..workers {
            let state = state.clone();
            let mut rx = tx.subscribe();

            tokio::spawn(async move {
                loop {
                    match rx.recv().await {
                        Ok(vote) => {
                            // Process the vote without blocking the HTTP request
                            let _ = process_vote(&state, vote).await;
                        }
                        Err(broadcast::error::RecvError::Lagged(_)) => {
                            // Too many messages, skip some but continue
                            continue;
                        }
                        Err(broadcast::error::RecvError::Closed) => {
                            // Channel is closed, exit worker
                            break;
                        }
                    }
                }
            });
        }

        VoteProcessor { tx: Some(tx) }
    }

    /// Enqueue a vote for async processing
    /// Returns true if enqueued successfully, false if queue is full
    pub async fn enqueue(&self, vote: VoteRequest) -> bool {
        if let Some(tx) = &self.tx {
            tx.send(vote).is_ok()
        } else {
            false
        }
    }
}

async fn process_vote(state: &AppState, vote: VoteRequest) {
    let mut polls = state.polls.write().await;

    let Some(poll) = polls.get_mut(&vote.poll_id) else {
        return;
    };

    if !poll.is_open {
        return;
    }

    // has this voter already voted?
    if poll.voters.contains(&vote.voter_id) {
        return;
    }

    // Find the option and increment its vote count
    if let Some(option) = poll.options.iter_mut().find(|opt| opt.id == vote.option_id) {
        option.votes += 1;
        poll.voters.insert(vote.voter_id);

        // Notify via WebSocket
        let _ = state.ws_tx.send(poll.clone());
    }
}
