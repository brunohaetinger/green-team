//imports
use axum::{
    extract::{WebSocketUpgrade, Path, State},
    http::StatusCode,
    routing::{get, post},
    response::{IntoResponse},
    Json, Router,
};
use axum::extract::ws::{WebSocket, Message};
use futures::{StreamExt};
use serde::Deserialize;

use std::{
    collections::{HashMap,HashSet},
    net::SocketAddr,
    sync::{Arc, atomic::AtomicU32},
};

use tokio::sync::{broadcast, RwLock};

use voting_system::{
    AppState, VoteRequest, Poll, PollId,
<<<<<<< 2026-03-02-rust-vs-go
    ApiError, OptionItem,
=======
    ApiError, CreatePollRequest, OptionItem, processor::VoteProcessor,
>>>>>>> main
};

// ENDPOINTS

// POST /vote -> cast a vote
pub async fn vote(
    State(state): State<AppState>, 
    Json(payload): Json<VoteRequest>
) -> (StatusCode, Json<ApiError>) {
<<<<<<< 2026-03-02-rust-vs-go
    eprintln!("[VOTE] Incoming vote - poll_id: {}, option_id: {}, voter_id: {}", 
             payload.poll_id, payload.option_id, payload.voter_id);

    let mut polls = state.polls.write().await;

    let Some(poll) = polls.get_mut(&payload.poll_id) else {
        // poll NOT FOUND
        eprintln!("[VOTE] ERROR: Poll {} not found", payload.poll_id);
        return (
            StatusCode::NOT_FOUND,
            Json(ApiError { message: "Poll não encontrada".into() })
        );
    };

    if !poll.is_open {
        // poll closed
        eprintln!("[VOTE] ERROR: Poll {} is closed", payload.poll_id);
        return (
            StatusCode::FORBIDDEN,
            Json(ApiError { message: "A votação está encerrada".into() })
        );
    }

    // has this voter already voted in this poll?
    if poll.voters.contains(&payload.voter_id) {
        // User has already voted
        eprintln!("[VOTE] ERROR: Voter {} already voted in poll {}", payload.voter_id, payload.poll_id);
=======
    // Quick validation - check if poll and option exist
    {
        let polls = state.polls.read().await;
        
        let Some(poll) = polls.get(&payload.poll_id) else {
            return (
                StatusCode::NOT_FOUND,
                Json(ApiError { message: "Poll não encontrada".into() })
            );
        };

        if !poll.is_open {
            return (
                StatusCode::FORBIDDEN,
                Json(ApiError { message: "A votação está encerrada".into() })
            );
        }

        // Check if option exists
        if !poll.options.iter().any(|opt| opt.id == payload.option_id) {
            return (
                StatusCode::BAD_REQUEST,
                Json(ApiError { message: "Opção não encontrada nessa poll".into() })
            );
        }
    }

    // Enqueue the vote for async processing
    if !state.processor.enqueue(payload) {
        eprintln!("! Failed to enqueue vote");
>>>>>>> main
        return (
            StatusCode::SERVICE_UNAVAILABLE,
            Json(ApiError { message: "Fila de votos cheia, tente novamente".into() })
        );
    }

<<<<<<< 2026-03-02-rust-vs-go
    // Find the option and increment its vote count
    if let Some(option) = poll.options.iter_mut().find(|opt| opt.id == payload.option_id) {
        option.votes += 1;
        let option_votes = option.votes;
        
        poll.voters.insert(payload.voter_id.clone());
        let total_voters = poll.voters.len();

        // Clone poll while holding the lock
        let poll_to_send = poll.clone();

        // Release the write lock by dropping it
        drop(polls);

        // Notify via WebSocket OUTSIDE the lock
        let _ = state.ws_tx.send(poll_to_send);

        eprintln!("[VOTE] SUCCESS: Vote registered for poll {} - total voters: {}, option {} votes: {}", 
                 payload.poll_id, total_voters, payload.option_id, option_votes);

        return (
            StatusCode::ACCEPTED,
            Json(ApiError { message: "Voto registrado com sucesso".into() })
        );
    }
        // option not found
        eprintln!("[VOTE] ERROR: Option {} not found in poll {}", payload.option_id, payload.poll_id);
        return (
            StatusCode::BAD_REQUEST,
            Json(ApiError { message: "Opção não encontrada nessa poll".into() })
        );
    
=======
    // Return 202 Accepted immediately (like Go implementation)
    (
        StatusCode::ACCEPTED,
        Json(ApiError { message: "Voto registrado na fila".into() })
    )
>>>>>>> main
}

// GET /polls -> list all polls
async fn list_polls(State(state): State<AppState>) -> Json<HashMap<PollId, Poll>> {
    let polls = state.polls.read().await;
    eprintln!("[LIST_POLLS] Retrieved {} polls", polls.len());
    Json(polls.clone())
}

// GET /polls/:poll_id -> details of a specific poll
async fn get_poll(
    State(state): State<AppState>,
    Path(poll_id): Path<PollId>,
) -> Result<Json<Poll>, StatusCode> {
    let polls = state.polls.read().await; 
    if let Some(poll) = polls.get(&poll_id) {
        eprintln!("[GET_POLL] Retrieved poll {} - voters: {}", poll_id, poll.voters.len());
        Ok(Json(poll.clone()))
    } else {
        eprintln!("[GET_POLL] ERROR: Poll {} not found", poll_id);
        Err(StatusCode::NOT_FOUND)
    }
}
//todo: add websocket endpoint

// GET /ws -> stream poll updates via WebSocket
async fn ws_handler(
    ws: WebSocketUpgrade,
    State(state): State<AppState>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, state))
}

// Socket handler
async fn handle_socket(mut socket: WebSocket, state: AppState) {
    // subscribe to broadcast channel
    let mut rx = state.ws_tx.subscribe();

    loop {
        tokio::select! {
            Ok(poll) = rx.recv() => {
                let msg = serde_json::to_string(&poll).unwrap();
                if socket.send(Message::Text(msg)).await.is_err() {
                    break;
                }
            }
            Some(Ok(msg)) = socket.next() => {
                if let Message::Close(_) = msg {
                    break;
                }
            }
        }
    }
}

// POST /polls -> create a new poll
#[derive(Debug, Deserialize)]
pub struct CreatePollInput {
    pub id: Option<PollId>,
    pub question: String,
    pub is_open: Option<bool>,
    pub options: Option<Vec<String>>,
}

async fn create_poll(
    State(state): State<AppState>,
    Json(payload): Json<CreatePollInput>,
) -> (StatusCode, Json<serde_json::Value>) {
    eprintln!("[CREATE_POLL] Creating poll: question='{}', options={}", payload.question, payload.options.as_ref().map(|o| o.len()).unwrap_or(0));
    
    let mut polls = state.polls.write().await;
    
    let poll_id = match payload.id {
        Some(id) => {
            if polls.contains_key(&id) {
                eprintln!("[CREATE_POLL] ERROR: Poll {} already exists", id);
                return (
                    StatusCode::CONFLICT,
                    Json(serde_json::json!({ "message": "Poll already exists" }))
                );
            }
            id
        },
        None => state.next_poll_id.fetch_add(1, std::sync::atomic::Ordering::SeqCst),
    };

    let mut next_option_id: u32 = 1;
    let options: Vec<OptionItem> = payload
        .options
        .unwrap_or_default()
        .into_iter()
        .map(|label| {
            let id = next_option_id;
            next_option_id += 1;
            OptionItem {
                id,
                label,
                votes: 0,
            }
        })
        .collect();

    let new_poll = Poll {
        id: poll_id,
        question: payload.question,
        is_open: payload.is_open.unwrap_or(true),
        options,
        voters: HashSet::new(),
    };

    eprintln!("[CREATE_POLL] SUCCESS: Poll {} created with {} options", poll_id, new_poll.options.len());

    polls.insert(poll_id, new_poll.clone());

    (StatusCode::CREATED, Json(serde_json::to_value(new_poll).unwrap()))
}

#[derive(Debug, Deserialize)]
pub struct AddOptionRequest {
    pub id: u32,
    pub poll_id: u32,
    pub label: String,
}

async fn add_option(
    State(state): State<AppState>,
    Json(payload): Json<AddOptionRequest>,
) -> (StatusCode, Json<serde_json::Value>) {
    eprintln!("[ADD_OPTION] Adding option {} to poll {} - label: '{}'", payload.id, payload.poll_id, payload.label);
    
    let mut polls = state.polls.write().await;
    if let Some(poll) = polls.get_mut(&payload.poll_id) {
        if poll.options.iter().any(|o| o.id == payload.id) {
            eprintln!("[ADD_OPTION] ERROR: Option {} already exists in poll {}", payload.id, payload.poll_id);
             return (
                StatusCode::CONFLICT,
                Json(serde_json::json!({ "message": "Option already exists" }))
            );
        }
        poll.options.push(OptionItem {
            id: payload.id,
            label: payload.label,
            votes: 0,
        });
        eprintln!("[ADD_OPTION] SUCCESS: Option {} added to poll {}", payload.id, payload.poll_id);
        return (StatusCode::CREATED, Json(serde_json::json!({ "message": "Option added" })));
    }
    eprintln!("[ADD_OPTION] ERROR: Poll {} not found", payload.poll_id);
    (StatusCode::NOT_FOUND, Json(serde_json::json!({ "message": "Poll not found" })))
}

// GET /health -> health check
async fn health_check() -> StatusCode {
    eprintln!("[HEALTH] Health check requested");
    StatusCode::OK
}

// GET /stats -> get processor stats
async fn get_stats(
    State(state): State<AppState>,
) -> Json<serde_json::Value> {
    let processed = state.processor.processed_count.load(std::sync::atomic::Ordering::Relaxed);
    Json(serde_json::json!({
        "votes_processed": processed,
    }))
}

// POST /options -> add option to an existing poll
async fn create_option(
    State(state): State<AppState>,
    Json(payload): Json<serde_json::Value>,
) -> (StatusCode, Json<serde_json::Value>) {
    // Try to parse poll_id as either number or string
    let poll_id: u32 = match payload["poll_id"].as_u64() {
        Some(id) => id as u32,
        None => match payload["poll_id"].as_str() {
            Some(s) => s.parse().unwrap_or(0),
            None => 0,
        }
    };
    
    let label = payload["label"].as_str().unwrap_or("").to_string();
    
    let mut polls = state.polls.write().await;
    
    if let Some(poll) = polls.get_mut(&poll_id) {
        let next_option_id = poll.options.iter().map(|o| o.id).max().unwrap_or(0) + 1;
        poll.options.push(OptionItem {
            id: next_option_id,
            label: label.clone(),
            votes: 0,
        });
        (StatusCode::CREATED, Json(serde_json::json!({ "id": next_option_id })))
    } else {
        (StatusCode::NOT_FOUND, Json(serde_json::json!({ "error": "poll not found" })))
    }
}

// MAIN
#[tokio::main]
async fn main() {
    eprintln!("Starting server......");

    // Initialize polls store
    let polls_map: HashMap<PollId, Poll> = HashMap::new();

    // shared polls store
    let polls = Arc::new(RwLock::new(polls_map));

    // WebSocket broadcast channel
    let (ws_tx, _ws_rx) = broadcast::channel(100);

    let next_poll_id = Arc::new(AtomicU32::new(2));
    
    // Create a placeholder processor (will be replaced immediately)
    let initial_state = AppState { 
        polls: polls.clone(), 
        ws_tx: ws_tx.clone(), 
        next_poll_id: next_poll_id.clone(),
        processor: Arc::new(VoteProcessor::new_empty()), 
    };
    
    // Initialize vote processor with async workers
    let processor = Arc::new(VoteProcessor::new(initial_state, 0)); // 0 = auto-calculated workers
    
    // Final application state with processor
    let state = AppState { 
        polls, 
        ws_tx, 
        next_poll_id,
        processor,
    };

    // build app with routes
    let app = Router::new()
        .route("/health", get(health_check))
        .route("/vote", post(vote))
        .route("/polls", get(list_polls))
        .route("/polls/:poll_id", get(get_poll))
        .route("/polls", post(create_poll))
<<<<<<< 2026-03-02-rust-vs-go
        .route("/options", post(add_option))
        .with_state(state.clone());

    // start server
    let addr = SocketAddr::from(([0, 0, 0, 0], 3000));
    eprintln!("🚀 Server running on http://{}", addr);
=======
        .route("/options", post(create_option))
        .route("/ws", get(ws_handler)) 
        .route("/stats", get(get_stats))
        .with_state(state.clone());

    // start server
    let addr = SocketAddr::from(([127, 0, 0, 1], 8080));
    println!("Server running on http://{}", addr);
>>>>>>> main

    // create TCP listener
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
    
}
