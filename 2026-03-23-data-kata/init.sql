CREATE TABLE IF NOT EXISTS sales (
    id SERIAL PRIMARY KEY,
    salesman_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL
);

INSERT INTO sales (salesman_id, product_id, quantity)
SELECT 
    floor(random() * 20 + 1)::int as salesman_id,
    floor(random() * 201 + 100)::int as product_id,
    floor(random() * 100 + 1)::int as quantity
FROM generate_series(1, 500);
