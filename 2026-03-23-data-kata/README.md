# Data Kata - Mar 23th, 2026

## Objective

Create a modern data pipeline with:

1. Ingestion from 3 different data sources:
	1. Relational database
	2. Filesystem
	3. Webservice/API
2. Modern processing with Flink
3. Data Lineage (chain inspection, tracing/tracking)
4. Observability
5. There must have been at least 2 pipelines:
	1. Top Sales per city
	2. Top salesman in the whole country
6. The final aggregated results must be in a dedicated DB and API

## Restrictions

1. Python
2. Redshift
3. Haddop

## Requirements

1. Everything should run in docker
2. The final result should be a docker-compose file

## Glossary

- WAL (Write-Ahead Logging): improves significantly the performance and concurrency due to writting changes into a separate file (-wal) before applying these on the main database. This permits simultaneous reads and writes.
- Debezium: open-source distributed platform for `Change Data Capture` (CDC) which monitor DB logs to capture inserts, updates and deletes in real time. Based on Kafka Connect, it transform these changes into durable events flow.
- Kafka Sink Connector: it's a component that streams data from Kafka topics to external destination systems. (sink is refered as the "target system")
- Apache Flink: Distributed processing engine for stream processing and batch.
- Amazon S3 (Amazon Simple Store Service): AWS cloud service to store files/objects into buckets;
