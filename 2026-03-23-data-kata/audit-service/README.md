# Audit Service

Spring Boot service that consumes sales events from Kafka for auditing and logging purposes.

## Overview

The audit service listens to the `sales-enriched` Kafka topic 

```
sales-enriched topic ──→ Audit Service ──→ S3
```

## Tech Stack

- Java 21
- Spring Boot 3.2.3
- Spring Kafka
- Gradle

## Development Setup

### Prerequisites

- Java 21
- Docker & Docker Compose

### 1. Start Required Infrastructure

Start Kafka and related services from the project root:

```bash
# Start Kafka broker and Kafka Connect
docker-compose up -d
```

### 3. Data Generation

For generating test sales events, refer to the **datagen** module in the project root. The datagen service produces messages to Kafka topics that this service consumes.

## Build & Run

### Build

```bash
./gradlew clean build
```

### Run Tests

```bash
./gradlew test
```

### Run Application

```bash
./gradlew bootRun
```

## Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8081 | HTTP server port |
| `spring.kafka.bootstrap-servers` | localhost:9092 | Kafka broker address |
| `spring.kafka.consumer.group-id` | audit-service-group | Consumer group ID |
| `spring.kafka.consumer.auto-offset-reset` | earliest | Start from earliest offset |

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `S3_BUCKET_NAME` | Yes | Target S3 bucket name |
| `AWS_DEFAULT_REGION` | Yes | AWS region (e.g., `us-east-1`) |
| `AWS_ACCESS_KEY_ID` | Yes | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | Yes | AWS secret key |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |

## Kafka Consumer

Consumes from topic: `sales-enriched`

### Expected Message Format

```json
{
  "salesman_id": 12,
  "salesman_name": "Amanda Souza",
  "sale_id": 1,
  "quantity": 3,
  "product_id": 1004,
  "store_id": 7,
  "city_name": "Sao Paulo",
  "store_name": "Store-SP-001",
  "sale_date": "2026-03-11T14:20:31Z",
  "country_name": "Brazil",
  "amount": "29.90"
}
```

## Project Structure

```
audit-service/
├── build.gradle
├── src/main/java/com/greenteam/
│   ├── AuditService.java           # Spring Boot application
│   ├── HealthController.java       # Health endpoint
│   ├── SalesEventConsumer.java     # Kafka consumer
│   ├── config/
│   │   └── S3Config.java           # AWS S3 client configuration
│   ├── model/
│   │   └── SalesEvent.java         # Event model with CSV serialization
│   └── service/
│       └── S3Service.java          # S3 persistence logic
├── src/main/resources/
│   └── application.properties      # Configuration
└── src/test/java/com/greenteam/
    ├── HealthControllerTest.java
    ├── SalesEventConsumerTest.java
    ├── model/
    │   └── SalesEventTest.java
    └── service/
        └── S3ServiceTest.java
```

## S3 Output Structure

Events are saved as CSV files organized by date, country, city, and sale ID:

```
s3://{bucket}/
└── {date}/
    └── {country}/
        └── {city}/
            └── {sale_id}.csv
```
