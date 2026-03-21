# Observability

This folder contains the observability stack for the Data Kata pipeline.

## Components

### 1. Prometheus & Grafana (Metrics Dashboard)
- **Role:** Collects (Prometheus) and visualizes (Grafana) metrics from the entire pipeline.
- **Prometheus URL:** [http://localhost:9090](http://localhost:9090)
- **Grafana URL:** [http://localhost:3001](http://localhost:3001) (Default user/pass: `admin`/`admin`)
- **Dashboards:**
    - **Pipeline Observability Dashboards:** A centralized view of the data pipeline health, performance, and reliability.

#### Understanding the Pipeline Observability Dashboard

The dashboard is divided into three main sections to help you monitor and troubleshoot the pipeline:

##### 1. E2E Bottleneck Check
*   **Graph:** **Throughput and CPU Load**
*   **Metrics:** 
    - `sum(rate(debezium_metrics_TotalNumberOfEventsSeen[1m]))` (Debezium Throughput)
    - `sum(rate(flink_taskmanager_job_task_operator_numRecordsInPerSecond[1m]))` (Flink Throughput)
    - `rate(flink_jobmanager_Status_JVM_CPU_Load[1m])` (Flink CPU Load)
*   **Y-Axis:**
    - **Throughput:** **Events per second**.
    - **Flink CPU Load:** **Percentage (0.0 to 1.0)**, where 1.0 represents 100% CPU usage.
*   **How to read:**
    - **Debezium Throughput:** Shows events seen by Debezium from the source database.
    - **Flink Throughput:** Shows the events per second processed by the Flink pipeline.
    - **Flink CPU Load:** Helps correlate if Flink processing power is the limiting factor.

---

## End-to-End Observability Flow

1.  **Generation:** Data sources (Postgres, Web Server) generate logs and data.
2.  **Processing:** Flink and Kafka process events, emitting technical logs and metrics.
3.  **Visualization (Grafana):** 
    - Use **Grafana** to monitor system health and technical metrics.

## How to use
The observability services are automatically started with the main pipeline:
```bash
docker compose up
```
To view metrics in Grafana:
1. Access [http://localhost:3001](http://localhost:3001).
2. Go to **Explore** and select the **Prometheus** datasource.
