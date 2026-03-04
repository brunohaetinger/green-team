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

use std::{
    collections::{HashMap,HashSet},
    net::SocketAddr,
    sync::{Arc, atomic::AtomicU32},
};

use tokio::sync::{broadcast, RwLock};

use voting_system::{
    AppState, VoteRequest, Poll, PollId,
    CreatePollRequest, OptionItem, processor::VoteProcessor,
};

// ENDPOINTS

// POST /vote -> cast a vote
pub async fn vote(
    State(state): State<AppState>, 
    Json(payload): Json<VoteRequest>
) -> StatusCode {
    // Quick validation - check if poll and option exist
    {
        let polls = state.polls.read().await;
        
        let Some(poll) = polls.get(&payload.poll_id) else {
            return StatusCode::NOT_FOUND;
        };

        if !poll.is_open {
            return StatusCode::FORBIDDEN;
        }

        // Check if option exists
        if !poll.options.iter().any(|opt| opt.id == payload.option_id) {
            return StatusCode::BAD_REQUEST;
        }
    }

    // Enqueue the vote for async processing
    if state.processor.enqueue(payload) {
        StatusCode::ACCEPTED
    } else {
        StatusCode::SERVICE_UNAVAILABLE
    }
}

// GET /polls -> list all polls
async fn list_polls(State(state): State<AppState>) -> Json<HashMap<PollId, Poll>> {
    let polls = state.polls.read().await;
    Json(polls.clone())
}

// GET /polls/:poll_id -> details of a specific poll
async fn get_poll(
    State(state): State<AppState>,
    Path(poll_id): Path<PollId>,
) -> Result<Json<Poll>, StatusCode> {
    let polls = state.polls.read().await; 
    if let Some(poll) = polls.get(&poll_id) {
        Ok(Json(poll.clone()))
    } else {
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
async fn create_poll(
    State(state): State<AppState>,
    Json(payload): Json<CreatePollRequest>,
) -> (StatusCode, Json<Poll>) {
    
    let poll_id = state.next_poll_id.fetch_add(1, std::sync::atomic::Ordering::SeqCst);
    let mut polls = state.polls.write().await;
    let mut next_option_id: u32 = 1;

    let options: Vec<OptionItem> = payload
        .options
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
        is_open: true,
        options,
        voters: HashSet::new(),
    };

    polls.insert(poll_id, new_poll.clone());

    return(
         StatusCode::CREATED,
         Json(new_poll)
    )
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
    println!("Starting server......");

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
        .route("/vote", post(vote))
        .route("/polls", get(list_polls))
        .route("/polls/:poll_id", get(get_poll))
        .route("/polls", post(create_poll))
        .route("/options", post(create_option))
        .route("/ws", get(ws_handler)) 
        .route("/stats", get(get_stats))
        .with_state(state.clone());

    // start server
    let addr = SocketAddr::from(([127, 0, 0, 1], 8080));
    println!("Server running on http://{}", addr);

    // create TCP listener
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
    
}
