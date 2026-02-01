# Modern Processing

## [Apache Spark](https://spark.apache.org/) x [Apache Flink](https://flink.apache.org/) x [Kafka Streams](https://kafka.apache.org/documentation/streams/)

| Category | Apache Spark | Apache Flink | Kafka Streams |
|---------|--------------|--------------|---------------|
| Processing Model | Batch + Micro-Batch | True Real-Time Streaming | Event-by-event stream processing inside your app |
| Latency | Seconds to hundreds of ms | Milliseconds | Milliseconds (depends on Kafka and app) |
| Ease of Use | Easier, larger ecosystem | More complex | Simple API, but requires Kafka-centric architecture |
| Lineage Support | Mature and well-integrated | Less mature | No native lineage, requires external tooling/instrumentation |
| Observability | Simple and widely supported | Advanced but requires expertise | App-centric metrics; you build/plug your own observability |
| Best Use Case | Analytics, ETL, periodic jobs | Real-time event-driven pipelines | Kafka-centric streaming microservices |

## Processing Model

### Apache Spark
- [Optimized for batch processing](https://spark.apache.org/docs/latest/streaming-programming-guide.html)
- [Supports micro-batch streaming](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#micro-batch-mode)
- Suitable when real-time latency is not required
- SQL-based transformations are simple and intuitive

If the rankings can be recalculated every few minutes, Spark is fully adequate.

### Apache Flink
- [Designed for true streaming](https://flink.apache.org/2015/02/09/introducing-flink-streaming/)
- Processes events individually as they arrive
- Excellent event-time and windowing semantics  
  https://nightlies.apache.org/flink/flink-docs-stable/docs/concepts/time/
- Lowest latency among streaming engines

If the system requires near real-time ranking, Flink is the better option.

### Kafka Streams
- [Lightweight stream processing library for Kafka](https://kafka.apache.org/documentation/streams/)
- Runs inside your application (no separate cluster)
- Event-by-event processing with local state stores
- Supports stateful operations and exactly-once semantics  
  https://kafka.apache.org/documentation/streams/core-concepts/

Best when all data flows through Kafka topics and you build streaming logic as part of a microservice.

## Integration With Data Sources

| Data Source | Spark | Flink | Kafka Streams |
|-------------|--------|--------|---------------|
| Relational Database | Excellent via JDBC | Great via CDC tools (e.g., Debezium) | Via Kafka Connect JDBC/CDC into topics |
| File System | Native support | Supported but less common | Via Kafka Connect/File connectors |
| Traditional Web Service | Batch ingestion | Best suited for event-driven ingestion | Requires producing events into Kafka first |

- Spark is better for batch-based ingestion from DBs and files.  
- Flink is superior when data arrives as continuous streams.  
- Kafka Streams requires all sources to be ingested into Kafka topics first (usually via [Kafka Connect](https://kafka.apache.org/documentation/#connect)).

## Data Lineage Requirements

### Spark
Mature integrations with:
- [Delta Lake](https://docs.delta.io/latest/index.html)
- [OpenLineage for Spark](https://openlineage.io/docs/integrations/spark/)
- [Apache Atlas Spark bridge](https://atlas.apache.org/1.2.0/Bridge-Spark.html)
- [Unity Catalog](https://docs.databricks.com/en/data-governance/unity-catalog/index.html)

✔ Widely adopted in enterprise governance

### Flink
✔ Supports lineage via [OpenLineage Flink integration](https://openlineage.io/docs/integrations/flink/)  
✖ Fewer templates, smaller ecosystem

### Kafka Streams
✖ No native lineage layer  
✔ Possible via ecosystem tooling and instrumentation (e.g. OpenLineage concepts for streaming)  
  https://openlineage.io/blog/streaming-philosophy/

If lineage is a strict requirement, Spark has the most mature and ready-to-use tooling.

## Observability

### Spark
✔ [Spark UI for job inspection](https://spark.apache.org/docs/latest/web-ui.html)

✔ Broad support:
- [Prometheus metrics](https://spark.apache.org/docs/latest/monitoring.html)
- [Grafana integration examples](https://grafana.com/docs/grafana-cloud/monitor-infrastructure/integrations/integration-reference/integration-spark/)
- [Datadog integration](https://www.datadoghq.com/dg/monitor/apache-spark)

✔ Easier to monitor for non-experts

### Flink
✔ Extremely detailed metrics  
  https://nightlies.apache.org/flink/flink-docs-stable/docs/ops/metrics/
✔ Per-operator and per-state observability  
✖ More complex to configure and interpret

### Kafka Streams
✔ Built-in metrics via Kafka clients  
  https://kafka.apache.org/documentation/#streams_monitoring
✔ Can expose/query local state via [Interactive Queries](https://kafka.apache.org/documentation/streams/developer-guide/interactive-queries/)
✖ Observability is application-centric (you must integrate with your monitoring stack)

Spark wins in ease, Flink in depth, Kafka Streams in flexibility inside microservices.

## Operational Complexity & Cost

### Spark
✔ Simpler setup  
✔ Cheaper for batch workloads  
✔ Works well on Kubernetes  
✖ Continuous streaming may require stronger infrastructure

### Flink
✔ Efficient for always-on streaming  
✖ Requires specialized tuning (state, checkpoints)  
  https://nightlies.apache.org/flink/flink-docs-stable/docs/ops/state/checkpoints/
✖ Higher maintenance cost for inexperienced teams

### Kafka Streams
✔ No separate processing cluster (runs inside your service)  
✔ Scales horizontally by adding more app instances  
  https://kafka.apache.org/documentation/streams/architecture
✖ Requires a well-operated Kafka cluster  
✖ Stateful app instances must be managed and monitored like critical services

Spark has lower operational overhead for analytics.  
Flink is best for dedicated real-time streaming platforms.  
Kafka Streams is ideal for Kafka-centric, always-on streaming microservices.


### Choice

Spark is the better choice in this project because:

- The problem is analytical, not explicitly real-time critical.
- Data lineage and observability are hard requirements, and Spark’s ecosystem is more mature.
- Ingestion from DBs, files, and traditional services aligns naturally with batch/micro-batch processing.
- ~~The team is not specialized in data streaming, making Spark safer to implement and operate.~~

We chose Apache Spark because it satisfies all functional requirements while minimizing operational complexity and delivery risk. Since sub-second latency is not a requirement, Spark’s micro-batch model is sufficient for analytical rankings and provides better governance and observability for the current team.