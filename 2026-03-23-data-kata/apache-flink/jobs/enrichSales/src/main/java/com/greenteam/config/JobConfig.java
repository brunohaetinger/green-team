package com.greenteam.config;

public final class JobConfig {

    public static final String BOOTSTRAP_SERVERS = "kafka:29092";
    public static final String SALES_TOPIC = "sales";
    public static final String STORES_TOPIC = "stores";
    public static final String SALESMANS_TOPIC = "salesmans";
    public static final String OUTPUT_TOPIC = "sales-enriched";

    // consumer group IDs are used to identify the consumer group that each source operator belongs to.
    // A consumer group is a group of consumers that work together to consume messages from a topic. 
    // Each consumer in the group is responsible for consuming messages from a subset of the partitions of the topic.
    public static final String SALES_CONSUMER_GROUP_ID = "enrich-sales-sales-source";
    public static final String STORES_CONSUMER_GROUP_ID = "enrich-sales-stores-source";
    public static final String SALESMANS_CONSUMER_GROUP_ID = "enrich-sales-salesmans-source";

    // will be 2 parallel instances for each operator,
    // and each instance will consume from a subset of partitions of the source topic.
    // For example, if the sales topic has 4 partitions,
    // each instance of the sales source operator will consume from 2 partitions.
    // This allows the job to process data in parallel and improve throughput.
    public static final int DEFAULT_PARALLELISM = 2;

    // if a sale event doesn't find its store or salesman match within 5 minutes, it will be discarded from the state
    public static final long PENDING_SALES_TTL_MS = 5 * 60 * 1000L;

    // checking interval is set to 30 seconds, which means that every 30 seconds, Flink will create a checkpoint of the job's state.
    // checking is a mechanism that allows Flink to save the state of the job at regular intervals, so that in case of a failure, the job can be restarted from the last checkpoint instead of starting from scratch.
    public static final long CHECKPOINT_INTERVAL_MS = 30_000L;
    // checking pause is set to 10 seconds, which means that after a checkpoint is completed, Flink will wait for at least 10 seconds before starting the next checkpoint.
    public static final long CHECKPOINT_MIN_PAUSE_MS = 10_000L;
    // checking timeout is set to 60 seconds, which means that if a checkpoint takes longer than 60 seconds to complete, it will be considered failed and will be discarded.
    public static final long CHECKPOINT_TIMEOUT_MS = 60_000L;

    // transactional ID prefix is used to identify the transactional ID that each sink operator will use when writing to the output topic.
    public static final String TRANSACTIONAL_ID_PREFIX = "enrich-sales-";
    // transaction timeout is set to 10 minutes, which means that if a transaction takes longer than 10 minutes to complete, it will be considered failed and will be aborted.
    public static final String TRANSACTION_TIMEOUT_MS = "600000";
}