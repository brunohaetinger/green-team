# Apache Flink

Apache Flink is a powerful and scalable distributed stream processing platform.

The setup includes:
- **JobManager**: Coordinates Flink job execution
- **TaskManager**: Executes Flink tasks
- **Kafka Integration**: It make a transformation from Source Topic and publish at the Sink topic

## Docker Compose Explanation

This project uses `docker-compose.yaml` to start a small Flink cluster connected to Kafka.

### Services

- `jobmanager`
  - Role: Cluster coordinator and control plane.
  - What it does: Receives job submissions, schedules tasks, tracks checkpoints/state metadata, and exposes the Flink Web UI.
  - Port mapping: `8082:8081` so dashboard is available at `http://localhost:8082`.

- `taskmanager`
  - Role: Worker node(s) that run the actual job subtasks.
  - What it does: Executes operators (map/filter/window/sink), handles task slots, and reports status/metrics back to JobManager.
  - Scaling: `scale: 1` currently starts one worker instance.

### Shared Configuration

- `FLINK_PROPERTIES`
  - `jobmanager.rpc.address: jobmanager`: tells workers how to find the JobManager.
  - `parallelism.default: 2`: default parallelism for jobs when not explicitly set.
  - `taskmanager.numberOfTaskSlots: 2`: execution slots available per TaskManager.

- `volumes`
  - `flink-data:/tmp`: persistent volume for Flink temp/state-related files.
  - `./jobs:/opt/flink/jobs`: mounts local jobs folder into containers for easy job submission.

- `networks`
  - `flink-network`: internal Flink communication network.
  - `kafka-network` (external): connects Flink services to your Kafka stack.

### Flink

```bash
# 1. First, start Kafka
# 2. Then start Flink (which will connect to Kafka)
docker-compose up -d
```

## Access Web Interface

- **Flink Dashboard**: http://localhost:8082
  - Visualize jobs, taskmanagers, metrics, and logs

## Monitor the Job

- Access the Flink Dashboard at http://localhost:8082
- Check job status, metrics, and logs
- View running and completed jobs

## Jar file
If you add your JAR file to the jobs/var folder when you start Apache Flink, it will be copied to the container and automatically discovered by the JobManager.