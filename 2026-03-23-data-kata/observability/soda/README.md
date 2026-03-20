# Soda Core - Data Quality

Soda Core is a command-line tool and Python library for data reliability. It helps you catch data quality issues before they affect downstream users.

## Configuration
- `configuration.yml`: Contains the connection details for the `sales_report` PostgreSQL database.
- `checks.yml`: Defines the "Expectations" (Data Contracts) for the tables `top_salesman` and `city_sales`.

## Running Checks
Soda Core is included in the main `docker compose up`. To trigger a manual scan, use:
```bash
docker compose run soda
```

## UI
The output of Soda scan is displayed in the terminal logs, which can be seen in the **Dozzle** UI at [http://localhost:8081](http://localhost:8081).
Alternatively, Soda scan results can be integrated into Soda Cloud for a full dashboard.
