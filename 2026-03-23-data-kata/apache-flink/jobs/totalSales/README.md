# totalSales

Apache Flink job that consumes raw sale events from Kafka, aggregates total sales per city over 1-minute tumbling windows, and publishes the results back to Kafka.

## Package structure

```
com.greenteam/
│
├── TotalSales.java                     ← Entry point. Wires the full pipeline.
│
├── config/
│   └── JobConfig.java                  ← All constants: topics, window size, Kafka config.
│
├── model/
│   ├── SaleEvent.java                  ← Parsed input event.
│   ├── CitySalesAccumulator.java       ← Mutable state held during a window.
│   └── CitySalesResult.java            ← Immutable output event written to Kafka.
│
├── operator/
│   ├── ParseSalesEvent.java            ← Parses JSON string into SaleEvent. Skips malformed records.
│   ├── CitySalesAggregate.java         ← Accumulates totals (amount, units, orders) per city.
│   └── CitySalesWindowFormatter.java   ← Builds the final JSON payload when the window closes.
│
├── serde/
│   └── CitySalesResultSerializer.java  ← Serializes CitySalesResult into Kafka key + value bytes.
│
└── util/
    └── JsonUtils.java                  ← Helpers for reading required/optional JSON fields.
```

---

## Data contracts

### Input — `sales-events`

| Field | Type | Description |
|---|---|---|
| `salesman_id` | string (UUID) | Salesman identifier |
| `sale_id` | string (UUID) | Unique sale identifier |
| `quantity` | integer | Number of units sold |
| `product_id` | string (UUID) | Product identifier |
| `city_id` | string (UUID) | City where the sale happened |
| `country_id` | string (UUID) | Country where the sale happened |
| `amount` | string (decimal) | Unit price — **not** multiplied by quantity |

```json
{
  "salesman_id": "8e95c9ef-9f63-4c8b-9f2e-46a0a1a7d2c1",
  "sale_id":     "51d6c8f8-b9f0-4f3d-9db6-8df2412db5b8",
  "quantity":    3,
  "product_id":  "f5884206-8361-4c6e-9bb0-6a9d8ca4a404",
  "city_id":     "2a2a4ef3-b69b-4fd1-9f27-f3c5e6eb31b8",
  "country_id":  "4b38fc9f-5f1c-46af-858f-5d9f9bb49a67",
  "amount":      "29.90"
}
```

> Records missing `city_id`, `sale_id`, `quantity ≤ 0`, or `amount` are silently skipped.

---

### Output — `total-sales`

| Field | Type | Description |
|---|---|---|
| `schema_version` | string | Payload version (`1.0`) |
| `aggregation_type` | string | Always `city_sales` |
| `city_id` | string | Aggregation key |
| `country_id` | string | First country seen in the window |
| `window_start` | ISO-8601 | Window opening timestamp |
| `window_end` | ISO-8601 | Window closing timestamp |
| `total_amount` | decimal (2dp) | Sum of `quantity × amount` across all events |
| `total_units` | integer | Sum of `quantity` |
| `total_orders` | integer | Distinct `sale_id` count |
| `event_count` | integer | Raw event count (including duplicates) |
| `processed_at` | ISO-8601 | Wall-clock time when the window was emitted |

```json
{
  "schema_version":   "1.0",
  "aggregation_type": "city_sales",
  "city_id":          "2a2a4ef3-b69b-4fd1-9f27-f3c5e6eb31b8",
  "country_id":       "4b38fc9f-5f1c-46af-858f-5d9f9bb49a67",
  "window_start":     "2026-03-11T14:20:00Z",
  "window_end":       "2026-03-11T14:21:00Z",
  "total_amount":     9876.50,
  "total_units":      143,
  "total_orders":     59,
  "event_count":      62,
  "processed_at":     "2026-03-11T14:21:01Z"
}
```

Kafka record key: `city_id|window_end`

## Build

```bash
./gradlew clean build
```

Output fat jar: `build/libs/totalSales-1.0-SNAPSHOT-fat.jar`

---

## Run

### Copy jar to shared volume

```bash
cp build/libs/totalSales-1.0-SNAPSHOT-fat.jar ../jar/
```

### Submit to Flink cluster

```bash
docker exec -it flink-jobmanager flink run \
  -p 2 \
  --class com.greenteam.TotalSales \
  /opt/flink/jobs/totalSales-1.0-SNAPSHOT-fat.jar
```

| Flag | Value | Description |
|---|---|---|
| `--class` | `com.greenteam.TotalSales` | Entry point |
| `-p` | `2` | Parallelism (task slots to use) |

