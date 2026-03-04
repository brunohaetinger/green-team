# Data Kata - Mar 23th, 2026

## Glossary

- WAL (Write-Ahead Logging): improves significantly the performance and concurrency due to writting changes into a separate file (-wal) before applying these on the main database. This permits simultaneous reads and writes.
- Debezium: open-source distributed platform for `Change Data Capture` (CDC) which monitor DB logs to capture inserts, updates and deletes in real time. Based on Kafka Connect, it transform these changes into durable events flow.
