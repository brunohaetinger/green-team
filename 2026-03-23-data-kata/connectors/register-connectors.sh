#!/bin/sh

set -eu

CONNECT_URL="${CONNECT_URL:-http://kafka-connect:8083}"
CONNECTORS_DIR="${CONNECTORS_DIR:-/connectors}"
CONNECTOR_FILES=$(find "${CONNECTORS_DIR}" -type f -name '*.json' | sort)

AVAILABLE_PLUGINS=$(curl -sf "${CONNECT_URL}/connector-plugins")

if [ -z "${CONNECTOR_FILES}" ]; then
  echo "No connector JSON files found in ${CONNECTORS_DIR}"
  exit 0
fi

printf '%s\n' "${CONNECTOR_FILES}" | while IFS= read -r connector_file; do
  connector_class=$(tr -d '\n' < "${connector_file}" | sed -n 's/.*"connector.class"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')

  if [ -z "${connector_class}" ]; then
    echo "Skipping ${connector_file}: missing connector.class"
    continue
  fi

  if ! printf '%s' "${AVAILABLE_PLUGINS}" | grep -F '"class":"'"${connector_class}"'"' > /dev/null; then
    echo "Skipping ${connector_file}: plugin ${connector_class} is not installed in Kafka Connect"
    continue
  fi

  echo "Registering connector from ${connector_file}"

  http_code=$(curl -sS -o /tmp/connector-response.txt -w '%{http_code}' \
    -X POST "${CONNECT_URL}/connectors" \
    -H 'Content-Type: application/json' \
    --data @"${connector_file}")

  case "${http_code}" in
    200|201)
      echo "Registered ${connector_file}"
      ;;
    409)
      echo "Connector already exists for ${connector_file}; skipping"
      ;;
    *)
      echo "Failed to register ${connector_file} (HTTP ${http_code})"
      cat /tmp/connector-response.txt
      exit 1
      ;;
  esac
done