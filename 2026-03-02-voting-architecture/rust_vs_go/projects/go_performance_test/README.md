# Go Performance Test - Poll Service

This project is a high-performance polling service built in Go, designed for stress testing and performance benchmarking using k6 load testing.

## Overview

The poll service is a lightweight HTTP API that handles voting/polling operations. It's configured to support different storage backends (memory, PostgreSQL, or Redis) and includes performance optimization features like socket reuse.

## Prerequisites

- Go 1.24.0 or later
- Docker & Docker Compose (for containerized deployment)
- k6 (for load testing) - [Installation guide](https://k6.io/docs/getting-started/installation/)
- PostgreSQL (optional, if using database backend)
- Redis (optional, if using Redis backend)

## Quick Start

### Option 1: Run Locally with Go

1. **Install dependencies:**
   ```bash
   go mod download
   ```

2. **Start the server:**
   ```bash
   go run main.go
   ```
   The server will start on `http://localhost:8080` by default.

3. **Configure environment variables (optional):**
   Create a `.env` file in the project root:
   ```env
   PORT=8080
   STORE_BACKEND=memory
   ENABLE_REUSEPORT=1
   ```

### Option 2: Run with Docker Compose

1. **Build and start the service:**
   ```bash
   docker-compose up
   ```

2. **Configuration:**
   Environment variables can be set in the `docker-compose.yml` file or in a `.env` file.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Server port |
| `STORE_BACKEND` | memory | Storage backend: `memory`, `postgres`, or `redis` |
| `DB_URL` | - | PostgreSQL connection URL (required if `STORE_BACKEND=postgres`) |
| `REDIS_URL` | - | Redis connection URL (required if `STORE_BACKEND=redis`) |
| `REDIS_QUEUE_NAME` | votes | Redis queue name for vote processing |
| `ENABLE_REUSEPORT` | - | Set to `1` to enable SO_REUSEPORT for socket reuse optimization |

## k6 Load Testing

### Running the Default Test

The `k6/poll_ramp_10k.js` script performs a ramping test that gradually scales from 1,000 to 10,000 concurrent virtual users (VUs).

1. **Ensure the server is running:**
   ```bash
   go run main.go
   ```

2. **Run the k6 test in a separate terminal:**
   ```bash
   k6 run k6/poll_ramp_10k.js
   ```

### Test Configuration

The test includes the following stages:
- **Stage 1:** Ramp up to 1,000 VUs over 30 seconds
- **Stage 2:** Ramp up to 2,000 VUs over 30 seconds
- **Stage 3:** Ramp up to 10,000 VUs (or custom `TARGET_VUS`) over 60 seconds
- **Stage 4:** Ramp down to 0 VUs over 60 seconds

**Thresholds:**
- Failed requests must be less than 5% (`http_req_failed < 5%`)
- 95th percentile response time must be under 2 seconds (`p(95) < 2000ms`)

### Custom Test Parameters

You can customize the test by setting environment variables:

```bash
# Run with custom target VUs
k6 run -e TARGET_VUS=5000 k6/poll_ramp_10k.js

# Run with custom base URL
k6 run -e BASE_URL=http://localhost:8080 k6/poll_ramp_10k.js

# Run with custom poll and option IDs
k6 run \
  -e POLL_ID=custom-poll-id \
  -e OPTION_ID=custom-option-id \
  k6/poll_ramp_10k.js

# Run with sleep between requests
k6 run -e SLEEP_MS=100 k6/poll_ramp_10k.js

# Enable setup phase
k6 run -e USE_SETUP=1 k6/poll_ramp_10k.js
```

### Generate HTML Reports

The k6 script automatically generates an HTML report. The report will be saved as `summary.html` in the current directory after the test completes.

## Architecture

- **Server**: HTTP API built with Go's `net/http` package
- **Storage**: Pluggable backend system supporting:
  - In-memory storage (for development)
  - PostgreSQL (for persistence)
  - Redis (for distributed caching)
- **Performance**: Supports SO_REUSEPORT for efficient socket reuse under high load

## Performance Optimization Tips

1. **Enable Socket Reuse:**
   ```bash
   ENABLE_REUSEPORT=1 go run main.go
   ```

2. **Use Docker with Resource Limits:**
   Docker Compose includes CPU and memory limits. Adjust in `docker-compose.yml` as needed.

3. **Monitor System Resources:**
   Use `docker stats` (if using Docker) or system monitoring tools to track CPU and memory usage during tests.

4. **Scale k6 Runners:**
   For extremely high load tests, consider running k6 in distributed mode across multiple machines.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 already in use | Change the `PORT` environment variable or kill the process using the port |
| Socket reuse not working | Ensure `ENABLE_REUSEPORT=1` is set and your OS supports SO_REUSEPORT |
| k6 command not found | Install k6 from https://k6.io/docs/getting-started/installation/ |
| High failure rates in k6 test | Check server logs, verify database/cache connectivity, increase server resources |
| Connection refused errors | Ensure the server is running and accessible at the configured URL |

## Project Structure

```
.
├── main.go                 # Server entry point
├── go.mod                  # Go module definition
├── go.sum                  # Go module checksums
├── Dockerfile              # Container image definition
├── docker-compose.yml      # Docker Compose configuration
├── internal/
│   ├── api/               # API route handlers
│   ├── models/            # Data models
│   ├── processor/         # Request processing logic
│   ├── seed/              # Data seeding utilities
│   └── store/             # Storage backends
└── k6/
    └── poll_ramp_10k.js   # k6 load test script
```

## License

See the main project README for license information.
