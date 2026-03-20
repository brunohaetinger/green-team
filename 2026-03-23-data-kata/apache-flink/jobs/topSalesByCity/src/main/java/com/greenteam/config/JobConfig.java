package com.greenteam.config;

public final class JobConfig {
    public static final String BOOTSTRAP_SERVERS       = "kafka:29092";
    public static final String INPUT_TOPIC             = "sales-enriched";
    public static final String OUTPUT_TOPIC            = "top-sales";
    public static final String CONSUMER_GROUP_ID       = "sales-enriched-pipeline";
    public static final int    WINDOW_MINUTES          = 1;
    public static final String TRANSACTIONAL_ID_PREFIX = "top-sales-";
    public static final String TRANSACTION_TIMEOUT_MS  = "600000";
    public static final String OPEN_LINEAGE_URL = "http://marquez-api:4000/api/v1/lineage";
    public static final String JOB_NAME = "top-sales-by-city";
    public static final String JOB_NAMESPACE = "green-team-data-kata";
}

