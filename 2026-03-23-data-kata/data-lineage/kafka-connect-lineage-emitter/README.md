# Kafka Connect Lineage Emitter

This application polls Kafka Connect for connector configurations and emits lineage metadata to [Marquez](https://marquezproject.ai/).

- Polls Kafka Connect REST API for connector configs
- Extracts lineage (topics, tables) from connector configs
- Emits lineage to Marquez via its Java client
- `KAFKA_CONNECT_URL`: URL for Kafka Connect REST API
- `MARQUEZ_API_URL`: Marquez API base URL