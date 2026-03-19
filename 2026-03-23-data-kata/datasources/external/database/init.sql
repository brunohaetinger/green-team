CREATE TABLE sales (
    id serial PRIMARY KEY,
    salesman_id INT NOT NULL,
    store_id INT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    sale_date TIMESTAMPTZ NOT NULL, 
    product_id INT NOT NULL, 
    quantity INT NOT NULL
);