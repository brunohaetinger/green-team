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
    ApiError, OptionItem,
};

// ENDPOINTS

// POST /vote -> cast a vote
pub async fn vote(
    State(state): State<AppState>, 
    Json(payload): Json<VoteRequest>
) -> (StatusCode, Json<ApiError>) {

    let mut polls = state.polls.write().await;

    let Some(poll) = polls.get_mut(&payload.poll_id) else {
        // poll NOT FOUND
        return (
            StatusCode::NOT_FOUND,
            Json(ApiError { message: "Poll não encontrada".into() })
        );
    };

    if !poll.is_open {
        // poll closed
        return (
            StatusCode::FORBIDDEN,
            Json(ApiError { message: "A votação está encerrada".into() })
        );
    }

    // has this voter already voted in this poll?
    if poll.voters.contains(&payload.voter_id) {
        // User has already voted
        return (
            StatusCode::CONFLICT,
            Json(ApiError { message: "Usuário já votou nessa poll".into() })
        );
    }

    // Find the option and increment its vote count
    if let Some(option) = poll.options.iter_mut().find(|opt| opt.id == payload.option_id) {
        option.votes += 1;
        poll.voters.insert(payload.voter_id);

        // Notify via WebSocket
        let _ = state.ws_tx.send(poll.clone());

        return (
            StatusCode::ACCEPTED,
            Json(ApiError { message: "Voto registrado com sucesso".into() })
        );
    }
        // option not found
        return (
            StatusCode::BAD_REQUEST,
            Json(ApiError { message: "Opção não encontrada nessa poll".into() })
        );
    
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
    let mut polls = state.polls.write().await;
    
    let poll_id = match payload.id {
        Some(id) => {
            if polls.contains_key(&id) {
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
    let mut polls = state.polls.write().await;
    if let Some(poll) = polls.get_mut(&payload.poll_id) {
        if poll.options.iter().any(|o| o.id == payload.id) {
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
        return (StatusCode::CREATED, Json(serde_json::json!({ "message": "Option added" })));
    }
    (StatusCode::NOT_FOUND, Json(serde_json::json!({ "message": "Poll not found" })))
}

// GET /health -> health check
async fn health_check() -> StatusCode {
    StatusCode::OK
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

    //
    let next_poll_id = Arc::new(AtomicU32::new(2));
    
    // application state
    let state = AppState { polls, ws_tx, next_poll_id };

    // build app with routes
    let app = Router::new()
        .route("/health", get(health_check))
        .route("/vote", post(vote))
        .route("/polls", get(list_polls))
        .route("/polls/:poll_id", get(get_poll))
        .route("/ws", get(ws_handler)) 
        .route("/polls", post(create_poll))
        .route("/options", post(add_option))
        .with_state(state.clone());

    // start server
    let addr = SocketAddr::from(([0, 0, 0, 0], 3000));
    println!("🚀 Server running on http://{}", addr);

    // create TCP listener
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();

    //Adding poll for testing
    /*let _ = create_poll(
        State(state.clone()), 
        Json(CreatePollRequest {
            question: "Which language is your favorite?".into(),
            options: vec!["Rust".into(), "Go".into(), "Python".into()],
        })
    ).await;*/
    
}
