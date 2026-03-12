#!/bin/bash

connectors=("connector")

for connector in ${connectors[@]}; do
    echo ">> Starting $connector setup"
    curl -X POST --location "http://localhost:8083/connectors" \
        -H "Content-Type: application/json" \
        -H "Accept: application/json" \
        -d @$connector.json

    sleep 2
    echo ">> Completed $connector setup"
done