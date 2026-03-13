# Data Lineage with OpenLineage + Marquez

- UI: `http://localhost:3000`
- API: `http://localhost:5000`


## Services
- **marquez-db** (`localhost:5434`) - Marquez database
- **marquez-api** (`localhost:5000`) - API OpenLineage / metadados
- **marquez-web** (`http://localhost:3000`) - UI to vizualize jobs, datasets and run events

## Run
docker compose up -d marquez-db marquez-api marquez-web  


## Recreate flink jobs
```
docker compose up -d --force-recreate jobmanager taskmanager
```