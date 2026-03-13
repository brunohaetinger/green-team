## Building the Project

This job consumes `sales-enriched` and emits the top salesman for each `sale_date` inside the configured window.

**Run**:
```bash
./gradlew clean build
cp build/libs/topSalesman-1.0-SNAPSHOT-fat.jar \
  ../jar/

docker exec -it flink-jobmanager flink run \
  -p 2 \
  --class com.greenteam.TopSalesman \
  /opt/flink/jobs/topSalesman-1.0-SNAPSHOT-fat.jar
```

**Parameters:**
- `--class`: Main class to execute
- `-p 2`: Parallelism level (2 parallel instances)