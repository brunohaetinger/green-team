# Top Salesman

Apache Flink job that consumes `sales-enriched`, ranks salesmen by total sales per day over 1-minute tumbling windows, and publishes the top salesman back to Kafka.

## Package structure

```text
com.greenteam/
|
+-- TopSalesman.java                    <- Entry point. Wires the full pipeline.
|
+-- config/
|   +-- JobConfig.java                 <- All constants: topics, window size, Kafka config.
|
+-- model/
|   +-- SaleEvent.java                 <- Parsed input event.
|   +-- TopSalesmanResult.java         <- Immutable output event written to Kafka.
|
+-- operator/
|   +-- ParseSalesEvent.java           <- Parses JSON string into SaleEvent. Skips malformed records.
|   +-- TopSalesmanWindowFormatter.java <- Aggregates per salesman and selects the winner per day.
|
+-- serde/
|   +-- TopSalesmanResultSerializer.java <- Serializes TopSalesmanResult into Kafka key + value bytes.
|
+-- util/
    +-- JsonUtils.java                 <- Helpers for reading required/optional JSON fields.
```

---

## Data contracts

### Input — `sales-enriched`

This job reads the `sales-enriched` topic contract and uses the salesman and sale amount fields for ranking. The topic itself contains the full enriched payload below.

| Field | Type | Description |
|---|---|---|
| `salesman_id` | integer | Salesman identifier |
| `salesman_name` | string | Salesman display name |
| `sale_id` | integer | Unique sale identifier |
| `quantity` | integer | Number of units sold |
| `product_id` | integer | Product identifier |
| `store_id` | integer | Store identifier |
| `city_name` | string | City where the sale happened |
| `store_name` | string | Store where the sale happened |
| `sale_date` | string (`YYYY-MM-DD` or ISO timestamp) | Sale day (the parser truncates to `YYYY-MM-DD`) |
| `country_name` | string | Country where the sale happened |
| `amount` | string (decimal) | Unit price — **not** multiplied by quantity |

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

---

### Output — `top-salesman`

| Field | Type | Description |
|---|---|---|
| `salesman_id` | integer | Reliable salesman key |
| `salesman_name` | string | Winning salesman for the day |
| `sale_date` | date | Sales day used for ranking |
| `total_amount` | decimal (2dp) | Sum of `quantity x amount` for the winner |
| `total_units` | integer | Sum of units sold by the winner |

```json
{
  "salesman_id": 12,
  "salesman_name": "Amanda Souza",
  "sale_date": "2026-03-11",
  "total_amount": 12500.40,
  "total_units": 188
}
```

Kafka record key: `salesman_id|sale_date`

## Build

```bash
./gradlew clean shadowJar
```

Output fat jar: `build/libs/topSalesman-1.0-SNAPSHOT-fat.jar`

---

## Run

### Copy jar to shared volume

```bash
cp build/libs/topSalesman-1.0-SNAPSHOT-fat.jar ../jar/
```

### Submit to Flink cluster

```bash
docker exec -it flink-jobmanager flink run \
  -p 1 \
  --class com.greenteam.TopSalesman \
  /opt/flink/jobs/topSalesman-1.0-SNAPSHOT-fat.jar
```

| Flag | Value | Description |
|---|---|---|
| `--class` | `com.greenteam.TopSalesman` | Entry point |
| `-p` | `1` | Parallelism (task slots to use) |