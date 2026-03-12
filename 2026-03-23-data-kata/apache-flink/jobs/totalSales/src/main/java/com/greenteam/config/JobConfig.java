package com.greenteam.config;

public final class JobConfig {

    public static final String BOOTSTRAP_SERVERS       = "kafka:29092";
    public static final String INPUT_TOPIC             = "sales-events";
    public static final String OUTPUT_TOPIC            = "total-sales";
    public static final String CONSUMER_GROUP_ID       = "sales-events-pipeline";
    public static final int    WINDOW_MINUTES          = 1;
    public static final String TRANSACTIONAL_ID_PREFIX = "total-sales-";
    public static final String TRANSACTION_TIMEOUT_MS  = "600000";

    private JobConfig() {}
}

