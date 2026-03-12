CREATE TABLE IF NOT EXISTS total_sales (
    schema_version TEXT,
    aggregation_type TEXT,
    city_id TEXT,
    country_id TEXT,
    window_start TIMESTAMPTZ,
    window_end TIMESTAMPTZ,
    total_amount NUMERIC(18, 2),
    total_units BIGINT,
    total_orders BIGINT,
    event_count BIGINT,
    processed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS top_salesman (
    schema_version TEXT,
    aggregation_type TEXT,
    salesman_id TEXT,
    country_id TEXT,
    window_start TIMESTAMPTZ,
    window_end TIMESTAMPTZ,
    total_amount NUMERIC(18, 2),
    total_units BIGINT,
    total_orders BIGINT,
    event_count BIGINT,
    processed_at TIMESTAMPTZ
);