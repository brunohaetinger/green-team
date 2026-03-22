Generates fake data and writes directly to the following destinations:

- Writes **sales** data to the database in `datasources/external/database` (PostgreSQL).
- Writes **stores** data to `data/stores.csv` (CSV file).
- Writes **salesmans** data to the database in `datasources/external/web-server` (PostgreSQL).

### Run with Docker Compose

1. Make sure the required databases are running and on the same Docker network (`data-kata-net`).
2. In this directory, run:

	```bash
	docker compose up
	```
