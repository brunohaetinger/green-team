# Data Kata - Must create a modern Pipeline - Mar 23th, 2026

# Challenge
1. Ingestion for 3 different data sources (Relational DB, File system and Traditional WS-*)
2. Modern Processing with Spark, Flink or Kafka Streams
3. Data Lineage
4. Observability
5. Pipeline must have at least 2 pipelines:
    a. Top Sales per City
    b. Top Salesman in the whole country
6. The final Aggregated results must be in a dedicated DB and API
7. Restrictions
    a. Python
    b. Red-Shift
    c. Hadoop

## Glossary

- WAL (Write-Ahead Logging): improves significantly the performance and concurrency due to writting changes into a separate file (-wal) before applying these on the main database. This permits simultaneous reads and writes.
- Debezium: open-source distributed platform for `Change Data Capture` (CDC) which monitor DB logs to capture inserts, updates and deletes in real time. Based on Kafka Connect, it transform these changes into durable events flow.
- Kafka Sink Connector: it's a component that streams data from Kafka topics to external destination systems. (sink is refered as the "target system")
- Apache Flink: Distributed processing engine for stream processing and batch.
- Amazon S3 (Amazon Simple Store Service): AWS cloud service to store files/objects into buckets;
