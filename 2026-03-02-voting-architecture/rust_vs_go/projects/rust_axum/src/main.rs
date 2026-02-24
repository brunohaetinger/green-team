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
    ApiError, CreatePollRequest, OptionItem, processor::VoteProcessor,
};

// ENDPOINTS

// POST /vote -> cast a vote
pub async fn vote(
    State(state): State<AppState>, 
    Json(payload): Json<VoteRequest>
) -> (StatusCode, Json<ApiError>) {
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
    if !state.processor.enqueue(payload).await {
        return (
            StatusCode::SERVICE_UNAVAILABLE,
            Json(ApiError { message: "Fila de votos cheia, tente novamente".into() })
        );
    }

    // Return 202 Accepted immediately (like Go implementation)
    (
        StatusCode::ACCEPTED,
        Json(ApiError { message: "Voto registrado na fila".into() })
    )
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
        .route("/ws", get(ws_handler)) 
        .route("/polls", post(create_poll))
        .with_state(state.clone());

    // start server
    let addr = SocketAddr::from(([127, 0, 0, 1], 8080));
    println!("🚀 Server running on http://{}", addr);

    // create TCP listener
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
    
}
