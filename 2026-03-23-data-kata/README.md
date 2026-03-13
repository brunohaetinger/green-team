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

## Contracts

### Source topics

#### `sales`

Raw sales coming from the relational source.

| Field | Type | Description |
|---|---|---|
| `id` | string (UUID) | Sale identifier |
| `salesman_id` | string (UUID) | Salesman responsible for the sale |
| `store_id` | string (UUID) | Store where the sale happened |
| `amount` | number | Unit price |
| `sale_date` | ISO-8601 datetime string | Sale timestamp |
| `product_id` | string (UUID) | Product identifier |
| `quantity` | integer | Quantity sold |

```json
{
    "id": "51d6c8f8-b9f0-4f3d-9db6-8df2412db5b8",
    "salesman_id": "8e95c9ef-9f63-4c8b-9f2e-46a0a1a7d2c1",
    "store_id": "b9b9cb73-8f4a-4d5f-8f0e-064b6ae5f81d",
    "amount": 29.90,
    "sale_date": "2026-03-11T14:20:31Z",
    "product_id": "f5884206-8361-4c6e-9bb0-6a9d8ca4a404",
    "quantity": 3
}
```

#### `stores`

Store master data coming from filesystem ingestion.

| Field | Type | Description |
|---|---|---|
| `id` | string (UUID) | Store identifier |
| `name` | string | Store name |
| `city` | string | City name |
| `state` | string | State abbreviation |
| `country` | string | Country code |

```json
{
    "id": "b9b9cb73-8f4a-4d5f-8f0e-064b6ae5f81d",
    "name": "Store-SP-001",
    "city": "Sao Paulo",
    "state": "SP",
    "country": "BR"
}
```

#### `salesmans`

Salesman master data coming from the API source.

| Field | Type | Description |
|---|---|---|
| `id` | string (UUID) | Salesman identifier |
| `name` | string | Salesman name |
| `store_id` | string (UUID) | Store assigned to the salesman |

```json
{
    "id": "8e95c9ef-9f63-4c8b-9f2e-46a0a1a7d2c1",
    "name": "Amanda Souza",
    "store_id": "b9b9cb73-8f4a-4d5f-8f0e-064b6ae5f81d"
}
```

### Enriched topic

#### `sales-enriched`

Event produced after joining sale data with store and salesman context. This is the input consumed by the Flink jobs.

| Field | Type | Required by | Description |
|---|---|---|---|
| `salesman_id` | string (UUID) | Both jobs | Salesman identifier |
| `sale_id` | string (UUID) | Both jobs | Sale identifier |
| `quantity` | integer | Both jobs | Quantity sold |
| `product_id` | string (UUID) | `topSalesByCity` | Product identifier |
| `city_name` | string | `topSalesByCity` | City where the sale happened |
| `store_name` | string | `topSalesByCity` | Store where the sale happened |
| `sale_date` | string (`YYYY-MM-DD` or ISO timestamp) | `topSalesByCity` | Sale date; the job truncates to day |
| `country_id` | string | Optional | Country identifier |
| `amount` | string or number | Both jobs | Unit price |

```json
{
    "salesman_id": "8e95c9ef-9f63-4c8b-9f2e-46a0a1a7d2c1",
    "sale_id": "51d6c8f8-b9f0-4f3d-9db6-8df2412db5b8",
    "quantity": 3,
    "product_id": "f5884206-8361-4c6e-9bb0-6a9d8ca4a404",
    "city_name": "Sao Paulo",
    "store_name": "Store-SP-001",
    "sale_date": "2026-03-11T14:20:31Z",
    "country_id": "BR",
    "amount": "29.90"
}
```

### Aggregated outputs

#### `top-sales`

Output topic produced by the `topSalesByCity` job.

| Field | Type | Description |
|---|---|---|
| `city_name` | string | Aggregation key |
| `store_name` | string | Aggregation key |
| `sale_date` | date | Aggregation key (day) |
| `total_amount` | decimal (2dp) | Sum of `quantity × amount` |
| `total_units` | integer | Sum of `quantity` |

```json
{
    "city_name": "Sao Paulo",
    "store_name": "Store-SP-001",
    "sale_date": "2026-03-11",
    "total_amount": 9876.50,
    "total_units": 143
}
```

#### `top-salesman`

Output topic produced by the `topSalesman` job.

| Field | Type | Description |
|---|---|---|
| `schema_version` | string | Payload version |
| `aggregation_type` | string | Always `top_salesman` |
| `salesman_id` | string (UUID) | Winning salesman for the window |
| `country_id` | string | Country identifier, when available |
| `window_start` | timestamp | Window start |
| `window_end` | timestamp | Window end |
| `total_amount` | decimal (2dp) | Sum of `quantity × amount` |
| `total_units` | integer | Sum of units |
| `total_orders` | integer | Distinct order count |
| `event_count` | integer | Raw processed event count |
| `processed_at` | timestamp | Emission time |

```json
{
    "schema_version": "1.0",
    "aggregation_type": "top_salesman",
    "salesman_id": "8e95c9ef-9f63-4c8b-9f2e-46a0a1a7d2c1",
    "country_id": "BR",
    "window_start": "2026-03-11T14:20:00Z",
    "window_end": "2026-03-11T14:21:00Z",
    "total_amount": 12500.40,
    "total_units": 188,
    "total_orders": 73,
    "event_count": 73,
    "processed_at": "2026-03-11T14:21:01Z"
}
```

### Reporting database

#### `total_sales_window_deltas`

Append-only table written by the JDBC sink for `top-sales`.

| Column | Type |
|---|---|
| `city_name` | `TEXT` |
| `store_name` | `TEXT` |
| `sale_date` | `DATE` |
| `total_amount` | `NUMERIC(18, 2)` |
| `total_units` | `BIGINT` |

#### `total_sales_by_city`

Accumulated reporting table maintained by trigger in Postgres.

| Column | Type |
|---|---|
| `city_name` | `TEXT` |
| `store_name` | `TEXT` |
| `sale_date` | `DATE` |
| `total_amount` | `NUMERIC(18, 2)` |
| `total_units` | `BIGINT` |
| `updated_at` | `TIMESTAMPTZ` |