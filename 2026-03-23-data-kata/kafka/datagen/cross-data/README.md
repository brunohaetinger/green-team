# Cross-Topic Fake Data Generator

Simple Node.js generator that publishes fake but consistent data across these Kafka topics:

- `sales-fs` (stores)
- `sales-api` (salesmen)
- `sales-db` (sales)

## Why this generator

`DatagenConnector` is great for random data, but it does not enforce cross-topic references.
This script ensures IDs are consistent across topics:

- `sales-api.store_id` is always an existing `sales-fs.id`
- `sales-db.salesman_id` is always an existing `sales-api.id`
- `sales-db.store_id` is always the same store assigned to that salesman

```bash
cd kafka/datagen/cross-data
npm install
```

## Run

```bash
npm start
```

## Rate configuration

- `SALES_PER_SECOND` (default: `5`) -> `sales-db`
- `SALES_FS_PER_SECOND` (default: `1`) -> `sales-fs`
- `SALES_API_PER_SECOND` (default: `1`) -> `sales-api`