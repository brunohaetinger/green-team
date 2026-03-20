# Kafka Connect Lineage Emitter

This application polls Kafka Connect for connector configurations and emits lineage metadata to [Marquez](https://marquezproject.ai/).

## Features
- Polls Kafka Connect REST API for connector configs
- Extracts lineage (topics, tables) from connector configs
- Emits lineage to Marquez via its Java client
- Production-ready: error handling, environment config, idempotent namespace creation

## Usage

### Build
```sh
./gradlew build
```

### Run
```sh
KAFKA_CONNECT_URL=http://localhost:8083 \
MARQUEZ_URL=http://localhost:5000/api/v1 \
MARQUEZ_NAMESPACE=kafka-connect \
java -jar build/libs/kafka-connect-lineage-emitter.jar
```

- `KAFKA_CONNECT_URL`: URL for Kafka Connect REST API
- `MARQUEZ_URL`: Marquez API base URL
- `MARQUEZ_NAMESPACE`: Namespace for lineage jobs/datasets

## Requirements
- Java 21+ (tested with 25)
- Kafka Connect REST API
- Marquez server

## Extending
- Update `LineageService.mapToLineage()` to extract more fields as needed.
- Add more error handling or logging as required for your environment.
