# OpenLineage Java Client Integration

## 1. Add Dependency

Add the following to your `build.gradle.kts` dependencies block:

```kotlin
dependencies {
    implementation("io.openlineage:openlineage-java:1.12.0")
}
```

## 2. Basic Usage Example

```java
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.HttpTransport;

OpenLineage openLineage = new OpenLineage(OpenLineageClient.generateRunId());
OpenLineage.RunEvent runEvent = openLineage.newRunEventBuilder()
    .eventType(OpenLineage.RunEvent.EventType.START)
    .eventTime(OffsetDateTime.now())
    .run(openLineage.newRunBuilder().runId(UUID.randomUUID()).build())
    .job(openLineage.newJobBuilder().namespace("my-namespace").name("my-job").build())
    .build();

OpenLineageClient client = new OpenLineageClient(
    HttpTransport.builder().url("http://localhost:5000/api/v1/lineage").build()
);

client.emit(runEvent);
```

- Replace the URL with your Marquez/OpenLineage endpoint.
- Adjust job, namespace, and dataset details as needed.

## 3. Reference
- [OpenLineage Java Client Docs](https://openlineage.io/docs/client/java/usage)

