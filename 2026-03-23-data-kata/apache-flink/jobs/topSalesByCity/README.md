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
| `salesman_id` | integer | Salesman identifier |
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
  "sale_id":     1,
  "quantity":    3,
  "product_id":  1004,
  "store_id":    7,
  "city_name":   "Sao Paulo",
  "store_name":  "Loja Centro",
  "sale_date":   "2026-03-11",
  "country_name": "Brazil",
  "amount":      "29.90"
}
```

---

### Output — `top-sales`

| Field | Type | Description |
|---|---|---|
| `city_name` | string | Aggregation key |
| `store_id` | integer | Reliable store key |
| `store_name` | string | Aggregation key |
| `sale_date` | date | Aggregation key (day) |
| `total_amount` | decimal (2dp) | Sum of `quantity × amount` across all events |
| `total_units` | integer | Sum of `quantity` |

```json
{
  "city_name":        "Sao Paulo",
  "store_id":         7,
  "store_name":       "Loja Centro",
  "sale_date":        "2026-03-11",
  "total_amount":     9876.50,
  "total_units":      143
}
```

Kafka record key: `store_id|sale_date|window_end`

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

