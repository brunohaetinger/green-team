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

- **Go Service:** Accessible at `http://localhost:8080` (Health check: `/health`)
- **Rust Service:** Accessible at `http://localhost:3000` (Health check: `/health`)

## Performance Testing

A consolidated k6 script is provided to test both implementations.

### Run Load Test against Go

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e USE_SETUP=1 \
  -e REPORT_NAME=reports/k6-report-go.html \
  --summary-export reports/k6-summary-go.json \
  k6/poll_ramp_10k.js
```

### Run Load Test against Rust

```bash
k6 run \
  -e BASE_URL=http://localhost:3000 \
  -e USE_SETUP=1 \
  -e REPORT_NAME=reports/k6-report-rust.html \
  --summary-export reports/k6-summary-rust.json \
  k6/poll_ramp_10k.js
```

### Configuration Options

You can customize the load test using environment variables (`-e KEY=VALUE`):

- `BASE_URL`: The target service URL (default: `http://localhost:8080`).
- `USE_SETUP=1`: Automatically creates a poll and an option before starting the load test.
- `TARGET_VUS`: Sets the peak number of virtual users (default: `10000`).
- `SLEEP_MS`: Adds a sleep interval between requests for each VU (default: `0`).
- `REPORT_NAME`: The filename for the HTML report (default: `k6-report.html`).
- `POLL_ID`: The ID of the poll to vote on (default: `1`).
- `OPTION_ID`: The ID of the option to vote for (default: `1`).

### Test Reports

After running the load tests with the commands above, k6 generates reports in the `reports/` directory:

- **Go Reports:** `reports/k6-report-go.html` and `reports/k6-summary-go.json`.
- **Rust Reports:** `reports/k6-report-rust.html` and `reports/k6-summary-rust.json`.

You can open the HTML reports in your browser:
```bash
open reports/k6-report-go.html
open reports/k6-report-rust.html
```