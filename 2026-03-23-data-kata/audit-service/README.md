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

## Build & Run

### Prerequisites

- Java 21
- Kafka running on `localhost:9092`

### Build

```bash
./gradlew clean build
```

### Run

```bash
./gradlew bootRun
```

Or run the JAR directly:

```bash
java -jar build/libs/audit-service-1.0-SNAPSHOT.jar
```

### Run with Docker Kafka

When Kafka is running in Docker, use:

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 ./gradlew bootRun
```

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8081 | HTTP server port |
| `spring.kafka.bootstrap-servers` | localhost:9092 | Kafka broker address |
| `spring.kafka.consumer.group-id` | audit-service-group | Consumer group ID |
| `spring.kafka.consumer.auto-offset-reset` | earliest | Start from earliest offset |

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
│   ├── AuditService.java         # Spring Boot application
│   ├── HealthController.java     # Health endpoint
│   └── SalesEventConsumer.java   # Kafka consumer
└── src/main/resources/
    └── application.properties    # Configuration
```
