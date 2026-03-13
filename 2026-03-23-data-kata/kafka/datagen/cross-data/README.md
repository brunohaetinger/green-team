# Cross-Topic Fake Data Generator

Simple Node.js generator that publishes fake but consistent data across these Kafka topics:

- `stores` (stores)
- `salesmans` (salesmen)
- `sales` (sales)

## Why this generator

`DatagenConnector` is great for random data, but it does not enforce cross-topic references.
This script ensures integer IDs are consistent across topics:

- `salesmans.store_id` is always an existing `stores.id`
- `sales.salesman_id` is always an existing `salesmans.id`
- `sales.store_id` is always the same store assigned to that salesman

```bash
cd kafka/datagen/cross-data
npm install
```

## Run

```bash
npm start
```

## Rate configuration

- `SALES_PER_SECOND` (default: `5`) -> `sales`
- `SALES_FS_PER_SECOND` (default: `1`) -> `stores`
- `SALES_API_PER_SECOND` (default: `1`) -> `salesmans`