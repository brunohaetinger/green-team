## Building the Project

**Run**:
```bash
./gradlew clean build
cp build/libs/totalSales-1.0-SNAPSHOT-fat.jar \
  ../jar/

docker exec -it flink-jobmanager flink run \
  -p 2 \
  --class com.greenteam.TotalSales \
  /opt/flink/jobs/totalSales-1.0-SNAPSHOT-fat.jar
```

**Parameters:**
- `--class`: Main class to execute
- `-p 2`: Parallelism level (2 parallel instances)