FROM confluentinc/cp-kafka-connect:7.8.7
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest
RUN confluent-hub install --no-prompt debezium/debezium-connector-postgresql:latest
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-spooldir:latest
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-s3:latest