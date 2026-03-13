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

## Archicture

![](docs/Kata%20-%20Data%20Architecture.png)


## How to run

```
docker compose up
```

```
cd scripts/connector
```

```
./setup.sh
```

## Glossary

- WAL (Write-Ahead Logging): improves significantly the performance and concurrency due to writting changes into a separate file (-wal) before applying these on the main database. This permits simultaneous reads and writes.
- Debezium: open-source distributed platform for `Change Data Capture` (CDC) which monitor DB logs to capture inserts, updates and deletes in real time. Based on Kafka Connect, it transform these changes into durable events flow.
- Kafka Sink Connector: it's a component that streams data from Kafka topics to external destination systems. (sink is refered as the "target system")
- Apache Flink: Distributed processing engine for stream processing and batch.
- Amazon S3 (Amazon Simple Store Service): AWS cloud service to store files/objects into buckets;

## Payloads used by datasources
### PostgreSQL
```json
// Table name SALE
{
    "id"
    "salesman_id"
    "store_id"
    "amount"
    "sale_date"
    "product_id"
    "quantity"
}
```

### File System
```json
// STORE
{
    "id"
    "name"
    "city"
    "state"
    "country"
}
```

### API
```json
// SALESMAN
{
    "id"
    "name"
    "store_id"
}
```

## Payload that sales-enriched needs to receive
```json
{
  "salesman_id": "8e95c9ef-9f63-4c8b-9f2e-46a0a1a7d2c1",
  "salesman_name": "Name",
  "sale_id":     "51d6c8f8-b9f0-4f3d-9db6-8df2412db5b8",
  "quantity":    3,
  "product_id":  "f5884206-8361-4c6e-9bb0-6a9d8ca4a404",
  "city_id":     "2a2a4ef3-b69b-4fd1-9f27-f3c5e6eb31b8",
  "country_id":  "4b38fc9f-5f1c-46af-858f-5d9f9bb49a67",
  "amount":      "29.90"
}
```