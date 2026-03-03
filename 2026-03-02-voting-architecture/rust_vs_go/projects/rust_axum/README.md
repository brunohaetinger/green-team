# POC - Real time Voting System

# Challenge

Implement: Real time Voting System

## Solution proposal

## Do:

- [x] Implementation
- [x] Unit tests
- [x] Performance Test / Benchmarks
- [x] Proper Documentation
- [x] Expose Solution via REST API


## How to install

1. Install Rust:

```
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

2. Create projetct

```
cargo new hello-rust
```

3. Build/install dependencies

```
cargo build --release
```


## How to run the app w

1. Run project

```
cargo run
```


## How to Call API

1. Creating the poll

```
curl --location 'http://localhost:8080/polls' \
--header 'Content-Type: application/json' \
--data '{
    "question":"Which language is your favorite?",
    "options": ["Rust", "Go", "Java", "Python"]
}'
```

2. Vote

```
curl --location 'http://localhost:8080/vote' \
--header 'Content-Type: application/json' \
--data '{
    "poll_id": 2,
    "option_id": 3,
    "voter_id": "7559d194-a50f-45e5-8048-c9ff8d139d7c"
}'
```


## How to run tests

### Unit Tests

1. Ensure Rust is installed on your host
2. Ensure the app and redis are running
3. Execute the unity tests

```
cargo test
```

### K6 Tests
1. Inside the folder of K6
2. run command below:
```
k6 run poll_ramp_10k.js --env POLL_ID=5 --env OPTION_ID=1
```



## Other commands

### When you need to update the libraries version

```
cargo update -p redis
```



## References

- https://rust-lang.org/pt-BR/learn/get-started/
- https://crates.io/crates/redis (Repository central)
