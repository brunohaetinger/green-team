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
    - **Pipeline Observability Dashboards:** A centralized view of the data pipeline health, performance, and reliability.

#### Understanding the Pipeline Observability Dashboard

The dashboard is divided into three main sections to help you monitor and troubleshoot the pipeline:

##### 1. Pipeline Errors (Vector)
*   **Graph:** **Component Error Rate (Vector)**
*   **Metric:** `rate(vector_component_errors_total[1m])`
*   **Y-Axis:** **Errors per second**.
*   **How to read:**
    - This graph shows the number of errors per second for each stage of the pipeline:
        - **Connectors/Sources Errors:** Failures in fetching logs from Docker (e.g., permission issues).
        - **Transformers/Processing Errors:** Failures in parsing or transforming logs (e.g., invalid JSON).
        - **Sinks/Storage Errors:** Failures in sending data to the final destination (e.g., network timeout).
    - **Healthy:** All lines should be at 0.
    - **Issue:** A spike indicates that a specific stage is failing.

##### 2. E2E Bottleneck Check
*   **Graph:** **Throughput by Stage (events/sec)**
*   **Metrics:** 
    - `rate(vector_component_sent_events_total[1m])`
    - `rate(flink_jobmanager_Status_JVM_CPU_Load[1m])`
*   **Y-Axis:**
    - **Throughput (Lines):** **Events per second**.
    - **Flink CPU Load:** **Percentage (0.0 to 1.0)**, where 1.0 represents 100% CPU usage.
*   **How to read:**
    - This graph explicitly identifies the pipeline steps:
        - **1. Connectors (Debezium/Source):** Raw events entering the pipeline.
        - **2. Transformers (Log Processing):** Events successfully parsed/transformed.
        - **3. Sinks (Flink/Kafka/Storage):** Events sent to the final output.
    - **Bottleneck Detection:** If the throughput of "Connectors" is much higher than "Transformers", the bottleneck is in the parsing stage. If "Transformers" is high but "Sinks" is low, the issue is in the final delivery (e.g., slow sink).
    - **Flink CPU Load:** Helps correlate if Flink processing power is the limiting factor.

##### 3. Connectors Healthy & Volume
*   **Graph:** **Buffer Volume (Potential Congestion)**
*   **Metric:** `vector_buffer_received_events_total`
*   **Y-Axis:** **Total number of events** (accumulated in buffer).
*   **How to read:**
    - Tracks data accumulation in Vector's internal buffers for each stage:
        - **Connectors Buffer**: Data waiting to be processed.
        - **Transformers Buffer**: Data waiting to be sent to sinks.
        - **Sinks Buffer**: Data queued for final delivery.
    - **Healthy:** The buffer should be relatively stable or empty.
    - **Issue:** An upward trend in any buffer indicates that the *next* stage cannot keep up with the current data rate (congestion).

---

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
