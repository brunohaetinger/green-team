# Vector Observability

Vector is a high-performance observability data pipeline. In this project, it is configured to:
1. Collect logs from all Docker containers.
2. Parse JSON messages (from Debezium and Flink).
3. Export metrics in Prometheus format.

## Configuration
The configuration is located in `vector.toml`.

## UI
To see the logs being routed by Vector in a user-friendly way, you can use **Dozzle** at [http://localhost:8081](http://localhost:8081).
For metrics, Vector exposes a Prometheus endpoint at [http://localhost:9090/metrics](http://localhost:9090/metrics).
