# Enrich Sales

Apache Flink job that consumes `sales`, `stores`, and `salesmans`, enriches sale events with store and salesman context, and publishes the result to `sales-enriched`.

## Package structure

```text
com.greenteam/
|
+-- EnrichSales.java                   <- Entry point. Wires the full pipeline.
|
+-- config/
|   +-- JobConfig.java                 <- Topics, consumer groups, TTL, checkpoints and Kafka config.
|
+-- model/
|   +-- SalesEvent.java                <- Parsed sale fact.
|   +-- StoreEvent.java                <- Parsed store dimension.
|   +-- SalesmanEvent.java             <- Parsed salesman dimension.
|   +-- SaleWithStoreEvent.java        <- Intermediate event after store join.
|   +-- SalesEnrichedEvent.java        <- Final enriched event written to Kafka.
|   +-- PendingSalesByStore.java       <- Pending sales waiting for store dimension.
|   +-- PendingSalesBySalesman.java    <- Pending sales waiting for salesman dimension.
|
+-- operator/
|   +-- ParseSalesEvent.java           <- Parses `sales` messages. Skips malformed records.
|   +-- ParseStoreEvent.java           <- Parses `stores` messages. Skips malformed records.
|   +-- ParseSalesmanEvent.java        <- Parses `salesmans` messages. Skips malformed records.
|   +-- JoinSalesWithStore.java        <- Holds pending sales by `store_id` and joins when store data exists.
|   +-- JoinSalesWithSalesman.java     <- Holds pending sales by `salesman_id` and joins when salesman data exists.
|
+-- serde/
|   +-- SalesEnrichedSerializer.java   <- Serializes SalesEnrichedEvent into Kafka key + value bytes.
|
+-- util/
    +-- JsonUtils.java                 <- Helpers for extracting raw payloads and required fields.
```

---

## Data contracts

### Inputs — `sales`, `stores`, `salesmans`

#### `sales`

| Field | Type | Description |
|---|---|---|
| `id` | integer | Sale identifier |
| `salesman_id` | integer | Salesman responsible for the sale |
| `store_id` | integer | Store where the sale happened |
| `amount` | number | Unit price |
| `sale_date` | ISO-8601 datetime string | Sale timestamp |
| `product_id` | integer | Product identifier |
| `quantity` | integer | Quantity sold |

```json
{
  "id": 1,
  "salesman_id": 12,
  "store_id": 7,
  "amount": 29.90,
  "sale_date": "2026-03-11T14:20:31Z",
  "product_id": 1004,
  "quantity": 3
}
```

#### `stores`

| Field | Type | Description |
|---|---|---|
| `id` | integer | Store identifier |
| `name` | string | Store name |
| `city` | string | City name |
| `state` | string | State abbreviation |
| `country` | string | Country code |

```json
{
  "id": 7,
  "name": "Store-SP-001",
  "city": "Sao Paulo",
  "state": "SP",
  "country": "BR"
}
```

#### `salesmans`

| Field | Type | Description |
|---|---|---|
| `id` | integer | Salesman identifier |
| `name` | string | Salesman name |
| `store_id` | integer | Store assigned to the salesman |

```json
{
  "id": 12,
  "name": "Amanda Souza",
  "store_id": 7
}
```

---

### Output — `sales-enriched`

| Field | Type | Description |
|---|---|---|
| `salesman_id` | integer | Salesman identifier |
| `salesman_name` | string | Salesman display name |
| `sale_id` | integer | Unique sale identifier |
| `quantity` | integer | Quantity sold |
| `product_id` | integer | Product identifier |
| `store_id` | integer | Store identifier |
| `city_name` | string | City where the sale happened |
| `store_name` | string | Store where the sale happened |
| `sale_date` | string (`YYYY-MM-DD` or ISO timestamp) | Original sale timestamp |
| `country_name` | string | Country where the sale happened |
| `amount` | string (decimal) | Unit price |

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

Kafka record key: `sale_id`

## Runtime behavior

- Keeps the latest `stores` record in keyed state by `store_id`.
- Keeps the latest `salesmans` record in keyed state by `salesman_id`.
- Holds `sales` in pending state with TTL when a required dimension is missing.
- Emits the enriched event as soon as both dimensions are available.
- Increments metrics for malformed records, pending joins, late joins, and TTL expirations.

## Build

```bash
./gradlew clean shadowJar
```

Output fat jar: `build/libs/enrichSales-1.0-SNAPSHOT-fat.jar`

---

## Run

### Copy jar to shared volume

```bash
cp build/libs/enrichSales-1.0-SNAPSHOT-fat.jar ../jar/
```

### Submit to Flink cluster

```bash
docker exec -it flink-jobmanager flink run \
  -Dexecution.attached=true \
  -p 1 \
  --class com.greenteam.EnrichSales \
  /opt/flink/jobs/enrichSales-1.0-SNAPSHOT-fat.jar
```

| Flag | Value | Description |
|---|---|---|
| `--class` | `com.greenteam.EnrichSales` | Entry point |
| `-p` | `1` | Parallelism (task slots to use) |

## Flink Checkpoints & State Backend

Flink jobs periodically create snapshots (checkpoints) of their internal state to ensure fault tolerance and data consistency. If a failure occurs, Flink can restore the job from the latest checkpoint, avoiding data loss or duplication.

### Where Are Snapshots Stored?
- By default, snapshots are stored in the local filesystem of the JobManager container (e.g., `/tmp/flink`).
- The storage location can be configured via Flink's state backend settings.
- For production, it's recommended to use a distributed storage (S3, HDFS, NFS, etc.) for durability and scalability.

### Best Practices for State Backend
- **Development/Local:** Use the filesystem backend for simplicity.
- **Production/Distributed:** Use `RocksDBStateBackend` with checkpoints stored in S3 or HDFS. This backend is highly scalable and efficient for large state.
- S3 is ideal for cloud environments; HDFS is preferred in Hadoop clusters.

### Example Configuration
To configure the state backend and checkpoint directory in your Flink job:

```java
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.rocksdb.RocksDBStateBackend;

// For filesystem backend
env.setStateBackend(new FsStateBackend("file:///tmp/flink/checkpoints"));

// For RocksDB backend with S3
env.setStateBackend(new RocksDBStateBackend("s3://your-bucket/flink/checkpoints", true));
```

### Why Use Checkpoints?
- Guarantees exactly-once or at-least-once processing.
- Enables fast recovery from failures.
- Preserves operator and keyed state across restarts.

For more details, see:
- [Flink State Backends & Checkpoints](https://nightlies.apache.org/flink/flink-docs-master/docs/ops/state/state_backends/)
- [Flink Recovery & Fault Tolerance](https://nightlies.apache.org/flink/flink-docs-master/docs/ops/state/checkpoints/)