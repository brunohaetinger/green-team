# Observability

This folder contains the observability stack for the Data Kata pipeline.

## Components

### 1. Vector (Logging and Metrics)
- **Role:** Collects, parses, and routes logs from all Docker containers.
- **Location:** `./vector`
- **More info:** [Vector README](./vector/README.md)

### 2. Soda Core (Data Quality)
- **Role:** Scans the final Aggregated Database to ensure data integrity and compliance with contracts.
- **Location:** `./soda`
- **More info:** [Soda README](./soda/README.md)

### 3. Dozzle (Log UI)
- **Role:** Web-based UI to browse and search real-time logs from all components.
- **URL:** [http://localhost:8081](http://localhost:8081)

### 4. Prometheus & Grafana (Metrics Dashboard)
- **Role:** Collects (Prometheus) and visualizes (Grafana) metrics from the entire pipeline.
- **Prometheus URL:** [http://localhost:9090](http://localhost:9090)
- **Grafana URL:** [http://localhost:3001](http://localhost:3001) (Default user/pass: `admin`/`admin`)
- **Dashboards:**
    - **Pipeline Errors:** Monitor error rates across all components.
    - **E2E Bottleneck:** Visualize throughput (events/sec) at each stage to identify slow components.
    - **Connectors & Volume:** Track data volume and buffer status to prevent congestion.
    - **Data Flow Lineage:** For tracing data path, use **Marquez Web** at [http://localhost:3000](http://localhost:3000).

## End-to-End Observability Flow

1.  **Generation:** Data sources (Postgres, Web Server) generate logs and data.
2.  **Processing:** Flink and Kafka process events, emitting technical logs and metrics.
3.  **Collection (Vector):** Vector scrapes Docker logs, parses them, and exposes technical metrics (throughput, error counts) to Prometheus.
4.  **Validation (Soda):** Soda scans the final `report-db` and outputs Data Quality results (passed/failed checks).
5.  **Visualization (Grafana/Dozzle):** 
    - Use **Grafana** to monitor system health and technical metrics.
    - Use **Dozzle** to inspect Soda's detailed scan logs and troubleshoot pipeline errors.

## How to use
The observability services are automatically started with the main pipeline:
```bash
docker compose up
```
To run a manual data quality check:
```bash
docker compose run soda
```
To view metrics in Grafana:
1. Access [http://localhost:3001](http://localhost:3001).
2. Go to **Explore** and select the **Prometheus** datasource.
3. Query metrics like `vector_buffer_events_total` or `vector_component_errors_total`.
