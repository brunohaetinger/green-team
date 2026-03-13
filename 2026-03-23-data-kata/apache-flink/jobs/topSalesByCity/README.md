# Top Sales By City

Apache Flink job that consumes raw sale events from Kafka, aggregates total sales by city and store per day over 1-minute tumbling windows, and publishes the results back to Kafka.

## Package structure

```
com.greenteam/
│
├── TopSalesByCity.java                 ← Entry point. Wires the full pipeline.
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
│   ├── CitySalesAggregate.java         ← Accumulates totals (amount, units) per city, store and day.
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

### Input — `sales-enriched`

| Field | Type | Description |
|---|---|---|
| `salesman_id` | string (UUID) | Salesman identifier |
| `sale_id` | string (UUID) | Unique sale identifier |
| `quantity` | integer | Number of units sold |
| `product_id` | string (UUID) | Product identifier |
| `city_name` | string | City where the sale happened |
| `store_name` | string | Store where the sale happened |
| `sale_date` | string (`YYYY-MM-DD` or ISO timestamp) | Sale day (the parser truncates to `YYYY-MM-DD`) |
| `country_id` | string (UUID) | Country where the sale happened |
| `amount` | string (decimal) | Unit price — **not** multiplied by quantity |

```json
{
  "salesman_id": "8e95c9ef-9f63-4c8b-9f2e-46a0a1a7d2c1",
  "sale_id":     "51d6c8f8-b9f0-4f3d-9db6-8df2412db5b8",
  "quantity":    3,
  "product_id":  "f5884206-8361-4c6e-9bb0-6a9d8ca4a404",
  "city_name":   "Sao Paulo",
  "store_name":  "Loja Centro",
  "sale_date":   "2026-03-11",
  "country_id":  "4b38fc9f-5f1c-46af-858f-5d9f9bb49a67",
  "amount":      "29.90"
}
```

---

### Output — `top-sales`

| Field | Type | Description |
|---|---|---|
| `city_name` | string | Aggregation key |
| `store_name` | string | Aggregation key |
| `sale_date` | date | Aggregation key (day) |
| `total_amount` | decimal (2dp) | Sum of `quantity × amount` across all events |
| `total_units` | integer | Sum of `quantity` |

```json
{
  "city_name":        "Sao Paulo",
  "store_name":       "Loja Centro",
  "sale_date":        "2026-03-11",
  "total_amount":     9876.50,
  "total_units":      143
}
```

Kafka record key: `city_name|store_name|sale_date|window_end`

## Build

```bash
./gradlew clean build
```

Output fat jar: `build/libs/topSalesByCity-1.0-SNAPSHOT-fat.jar`

---

## Run

### Copy jar to shared volume

```bash
cp build/libs/topSalesByCity-1.0-SNAPSHOT-fat.jar ../jar/
```

### Submit to Flink cluster

```bash
docker exec -it flink-jobmanager flink run \
  -p 2 \
  --class com.greenteam.TopSalesByCity \
  /opt/flink/jobs/topSalesByCity-1.0-SNAPSHOT-fat.jar
```

| Flag | Value | Description |
|---|---|---|
| `--class` | `com.greenteam.TopSalesByCity` | Entry point |
| `-p` | `2` | Parallelism (task slots to use) |

