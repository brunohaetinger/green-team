# Rust vs Go Performance Comparison

This project compares the performance of a voting system implemented in both Rust (using Axum) and Go.

## Implementation Notes
- Concurrency is easy to manage with goroutines in Go.
- Go has lower complexity overall.
- Offering reliable libraries for building web services.
- While Rust can offer more raw performance, Go is already fast enough for almost all of our real-time scenarios.

## Getting Started

### Prerequisites
- Docker and Docker Compose
- [k6](https://k6.io/) (for running load tests)

### Running the Services

You can start both the Go and Rust services simultaneously using Docker Compose from the `projects` directory:

```bash
cd 2026-03-02-voting-architecture/rust_vs_go/projects
docker-compose up --build
```

- **Go Service:** Accessible at `http://localhost:8080`
- **Rust Service:** Accessible at `http://localhost:3000`

## Performance Testing

A consolidated k6 script is provided to test both implementations.

### Run Load Test against Go

```bash
k6 run -e BASE_URL=http://localhost:3000 -e USE_SETUP=1 2026-03-02-voting-architecture/rust_vs_go/projects/k6/poll_ramp_10k.js
```

### Run Load Test against Rust

```bash
k6 run -e BASE_URL=http://localhost:8080 -e USE_SETUP=1 2026-03-02-voting-architecture/rust_vs_go/projects/k6/poll_ramp_10k.js
```

### Configuration Options

You can customize the load test using environment variables (`-e KEY=VALUE`):

- `BASE_URL`: The target service URL (default: `http://localhost:8080`).
- `USE_SETUP=1`: Automatically creates a poll and an option before starting the load test.
- `TARGET_VUS`: Sets the peak number of virtual users (default: `10000`).
- `SLEEP_MS`: Adds a sleep interval between requests for each VU (default: `0`).