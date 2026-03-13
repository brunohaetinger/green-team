## Generate fake data (Datagen)

Use Kafka Connect Datagen to continuously publish fake messages to `sales-enriched`.

### Prerequisites

- Kafka broker reachable (examples below use `localhost:9092`)
- Kafka Connect running (REST API at `http://localhost:8083`)
- Datagen plugin installed in Connect (`io.confluent.kafka.connect.datagen.DatagenConnector`)

#### Install datagen
```bash
docker exec kafka-connect confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:0.7.0
docker restart kafka-connect
docker exec kafka-connect find /usr/share/confluent-hub-components -name "kafka-connect-datagen-*.jar"
```

### Generate `connector.json`

Generate the connector payload based on `sales-enriched-schema.avsc`

```bash
SCHEMA=$(cat sales-enriched-schema.avsc | tr -d '\n' | sed 's/"/\\"/g') && \
cat > sales-enriched-connector.json <<EOF
{
  "name": "sales-enriched-datagen",
  "config": {
    "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
    "tasks.max": "1",
    "kafka.topic": "sales-enriched",
    "max.interval": "100",
    "iterations": "-1",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "schema.string": "$SCHEMA"
  }
}
EOF
```

### Create the Datagen connector

```bash
curl -X POST "http://localhost:8083/connectors" \
  -H "Content-Type: application/json" \
  -d @sales-enriched-connector.json
```

### Check connector status

```bash
curl "http://localhost:8083/connectors/sales-enriched-datagen/status"
```

### Connector controls

```bash
curl -X PUT "http://localhost:8083/connectors/sales-enriched-datagen/pause"
curl -X PUT "http://localhost:8083/connectors/sales-enriched-datagen/resume"
curl -X DELETE "http://localhost:8083/connectors/sales-enriched-datagen"
```

### Datagen tuning

| Config | Default | Effect |
|---|---|---|
| `max.interval` | `100` ms | Lower value increases events/second |
| `tasks.max` | `1` | Higher value increases parallel generation |
| `iterations` | `-1` | `-1` means infinite; set a number for fixed-size batches |

---