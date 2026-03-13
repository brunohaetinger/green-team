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

Store master data coming from filesystem ingestion.

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

Salesman master data coming from the API source.

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

### Enriched topic

#### `sales-enriched`

Event produced after joining sale data with store and salesman context. This is the input consumed by the Flink jobs.

| Field | Type | Required by | Description |
|---|---|---|---|
| `salesman_id` | integer | Both jobs | Salesman identifier |
| `salesman_name` | string | `topSalesman` | Salesman display name |
| `sale_id` | integer | Both jobs | Sale identifier |
| `quantity` | integer | Both jobs | Quantity sold |
| `product_id` | integer | `topSalesByCity` | Product identifier |
| `store_id` | integer | Optional | Store identifier |
| `city_name` | string | `topSalesByCity` | City where the sale happened |
| `store_name` | string | `topSalesByCity` | Store where the sale happened |
| `sale_date` | string (`YYYY-MM-DD` or ISO timestamp) | Both jobs | Sale date; the job truncates to day |
| `country_name` | string | Optional | Country name |
| `amount` | string or number | Both jobs | Unit price |

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
| `salesman_name` | string | Winning salesman for the day |
| `sale_date` | date | Sales day used for the ranking |
| `total_amount` | decimal (2dp) | Sum of `quantity × amount` |
| `total_units` | integer | Sum of units |

```json
{
    "salesman_name": "Amanda Souza",
    "sale_date": "2026-03-11",
    "total_amount": 12500.40,
    "total_units": 188
}
```

### Reporting database

#### `total_sales_by_city_window_deltas`

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

#### `top_salesman_window_deltas`

Append-only table written by the JDBC sink for `top-salesman`.

| Column | Type |
|---|---|
| `salesman_name` | `TEXT` |
| `sale_date` | `DATE` |
| `total_amount` | `NUMERIC(18, 2)` |
| `total_units` | `BIGINT` |

#### `top_salesman`

Accumulated reporting table maintained by trigger in Postgres.

| Column | Type |
|---|---|
| `salesman_name` | `TEXT` |
| `sale_date` | `DATE` |
| `total_amount` | `NUMERIC(18, 2)` |
| `total_units` | `BIGINT` |
| `updated_at` | `TIMESTAMPTZ` |