-- Append table: receives one row per Flink window result
CREATE TABLE IF NOT EXISTS total_sales_by_city_window_deltas (
    city_name        TEXT NOT NULL,
    store_name       TEXT NOT NULL,
    sale_date        DATE NOT NULL,
    total_amount     NUMERIC(18, 2) NOT NULL,
    total_units      BIGINT NOT NULL
);

-- Accumulated table: one row per (city_name, store_name, sale_date)
CREATE TABLE IF NOT EXISTS total_sales_by_city (
    city_name    TEXT NOT NULL,
    store_name   TEXT NOT NULL,
    sale_date    DATE NOT NULL,
    total_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    total_units  BIGINT NOT NULL DEFAULT 0,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_total_sales_by_city PRIMARY KEY (city_name, store_name, sale_date)
);

-- Function: accumulates deltas from each window event into total_sales_by_city
CREATE OR REPLACE FUNCTION fn_accumulate_total_sales()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO total_sales_by_city (
        city_name, store_name, sale_date,
        total_amount, total_units, updated_at
    )
    VALUES (
        NEW.city_name, NEW.store_name, NEW.sale_date,
        NEW.total_amount, NEW.total_units, now()
    )
    ON CONFLICT (city_name, store_name, sale_date)
    DO UPDATE SET
        total_amount = total_sales_by_city.total_amount + EXCLUDED.total_amount,
        total_units  = total_sales_by_city.total_units  + EXCLUDED.total_units,
        updated_at   = now();

    RETURN NEW;
END;
$$;

-- Trigger: fires after each insert on the append table
DROP TRIGGER IF EXISTS trg_accumulate_total_sales ON total_sales_by_city_window_deltas;

CREATE TRIGGER trg_accumulate_total_sales
AFTER INSERT ON total_sales_by_city_window_deltas
FOR EACH ROW
EXECUTE FUNCTION fn_accumulate_total_sales();

ALTER TABLE IF EXISTS top_salesman RENAME TO top_salesman_window_deltas;

CREATE TABLE IF NOT EXISTS top_salesman_window_deltas (
    salesman_name TEXT NOT NULL,
    sale_date DATE NOT NULL,
    total_amount NUMERIC(18, 2) NOT NULL,
    total_units BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS top_salesman (
    salesman_name TEXT NOT NULL,
    sale_date DATE NOT NULL,
    total_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    total_units BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_top_salesman PRIMARY KEY (salesman_name, sale_date)
);

CREATE OR REPLACE FUNCTION fn_accumulate_top_salesman()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO top_salesman (
        salesman_name, sale_date,
        total_amount, total_units, updated_at
    )
    VALUES (
        NEW.salesman_name, NEW.sale_date,
        NEW.total_amount, NEW.total_units, now()
    )
    ON CONFLICT (salesman_name, sale_date)
    DO UPDATE SET
        total_amount = top_salesman.total_amount + EXCLUDED.total_amount,
        total_units  = top_salesman.total_units  + EXCLUDED.total_units,
        updated_at   = now();

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_accumulate_top_salesman ON top_salesman_window_deltas;

CREATE TRIGGER trg_accumulate_top_salesman
AFTER INSERT ON top_salesman_window_deltas
FOR EACH ROW
EXECUTE FUNCTION fn_accumulate_top_salesman();

INSERT INTO top_salesman (
    salesman_name,
    sale_date,
    total_amount,
    total_units,
    updated_at
)
SELECT
    salesman_name,
    sale_date,
    SUM(total_amount) AS total_amount,
    SUM(total_units) AS total_units,
    now()
FROM top_salesman_window_deltas
GROUP BY salesman_name, sale_date
ON CONFLICT (salesman_name, sale_date)
DO UPDATE SET
    total_amount = EXCLUDED.total_amount,
    total_units = EXCLUDED.total_units,
    updated_at = now();