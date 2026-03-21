FROM confluentinc/cp-kafka-connect:7.8.7
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest
RUN confluent-hub install --no-prompt debezium/debezium-connector-postgresql:latest
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-spooldir:latest

USER root
RUN curl -fsSL https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.20.0/jmx_prometheus_javaagent-0.20.0.jar \
    -o /usr/share/java/jmx_prometheus_javaagent.jar && \
    chmod 644 /usr/share/java/jmx_prometheus_javaagent.jar
COPY jmx-exporter-config.yml /opt/jmx-exporter-config.yml
USER appuser