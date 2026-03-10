# Sink Connector

## Register total-sales sink

```bash
curl -X POST http://localhost:8084/connectors \
  -H "Content-Type: application/json" \
  --data @connectors/total-sales-postgres-sink.json
```

Check connector status:

```bash
curl http://localhost:8084/connectors/<CONNECTOR_IDENTIFIER>/status
```
