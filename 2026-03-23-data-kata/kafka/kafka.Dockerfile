# Stage 1: Fetch the JMX Exporter agent
FROM alpine:3.19 AS fetcher
RUN apk add --no-cache curl
RUN curl -fsSL https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.20.0/jmx_prometheus_javaagent-0.20.0.jar \
    -o /tmp/jmx_prometheus_javaagent.jar

# Stage 2: Final Kafka image
FROM confluentinc/cp-kafka:7.8.7
USER root
COPY --from=fetcher /tmp/jmx_prometheus_javaagent.jar /usr/share/java/jmx_prometheus_javaagent.jar
RUN chmod 644 /usr/share/java/jmx_prometheus_javaagent.jar
USER appuser
