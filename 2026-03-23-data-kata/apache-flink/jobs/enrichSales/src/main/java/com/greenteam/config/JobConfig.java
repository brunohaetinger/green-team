package com.greenteam.config;

public final class JobConfig {

    public static final String BOOTSTRAP_SERVERS            = "kafka:29092";
    public static final String SALES_TOPIC                  = "sales";
    public static final String STORES_TOPIC                 = "stores";
    public static final String SALESMANS_TOPIC              = "salesmans";
    public static final String OUTPUT_TOPIC                 = "sales-enriched";

    public static final String SALES_CONSUMER_GROUP_ID      = "enrich-sales-sales-source";
    public static final String STORES_CONSUMER_GROUP_ID     = "enrich-sales-stores-source";
    public static final String SALESMANS_CONSUMER_GROUP_ID  = "enrich-sales-salesmans-source";

    public static final int    DEFAULT_PARALLELISM          = 2;
    public static final long   PENDING_SALES_TTL_MS         = 5 * 60 * 1000L;

    public static final long   CHECKPOINT_INTERVAL_MS       = 30_000L;
    public static final long   CHECKPOINT_MIN_PAUSE_MS      = 10_000L;
    public static final long   CHECKPOINT_TIMEOUT_MS        = 60_000L;

    public static final String TRANSACTIONAL_ID_PREFIX      = "enrich-sales-";
    public static final String TRANSACTION_TIMEOUT_MS       = "600000";

    private JobConfig() {}
}