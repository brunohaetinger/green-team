## Building the Project

```bash
mvn clean package
```

**Run**:
```bash
mvn clean package
docker cp target/topSalesman-1.0-SNAPSHOT-fat.jar flink-jobmanager:/opt/flink/jobs/
docker exec -it flink-jobmanager flink run \
  -p 2 \
  --class com.greenteam.TopSalesman \
  /opt/flink/jobs/topSalesman-1.0-SNAPSHOT-fat.jar
```

**Parameters:**
- `--class`: Main class to execute
- `-p 2`: Parallelism level (2 parallel instances)