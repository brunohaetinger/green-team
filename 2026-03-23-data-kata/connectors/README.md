# Kafka Connectors — Monitoring & Troubleshooting

This guide explains how to check the status, list, and logs of Kafka Connect connectors, and provides useful commands for troubleshooting. All commands assume Kafka Connect is running on localhost:8083 and Docker is used for container management.

## List All Connectors

To list all registered connectors:

```sh
curl http://localhost:8083/connectors
```

## Check Connector Status

To check the status of a specific connector:

```sh
curl http://localhost:8083/connectors/<connector_name>/status
```

- `state` should be `RUNNING`.
- If `FAILED` or `PAUSED`, check logs for errors.

## View Connector Configuration

To see the configuration of a connector:

```sh
curl http://localhost:8083/connectors/<connector_name>
```

## View Connector Logs

All connector logs are available in the Kafka Connect container logs:

```sh
docker logs kafka-connect --tail 100
```

To filter logs for a specific connector:

```sh
docker logs kafka-connect | grep <connector_name>
```