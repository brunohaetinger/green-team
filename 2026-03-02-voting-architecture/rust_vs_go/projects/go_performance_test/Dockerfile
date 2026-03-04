# Multi-stage build: build static Go binary, then run in a minimal image

# 1) Builder
FROM golang:1.24-alpine AS builder
WORKDIR /app

COPY go.mod go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod \
    go mod download

# Copy source
COPY . .

ENV CGO_ENABLED=0 GOOS=linux GOARCH=amd64
RUN --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    go build -o /bin/poll ./main.go

# 2) Runtime
FROM alpine:3.20
RUN adduser -D -H -u 10001 appuser
USER appuser
WORKDIR /home/appuser

# Default port
ENV PORT=8080
EXPOSE 8080

COPY --from=builder /bin/poll /usr/local/bin/poll

# Minimal health check: hit swagger.json (served by the app)
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD wget -qO- http://127.0.0.1:${PORT}/swagger.json >/dev/null 2>&1 || exit 1

ENTRYPOINT ["/usr/local/bin/poll"]