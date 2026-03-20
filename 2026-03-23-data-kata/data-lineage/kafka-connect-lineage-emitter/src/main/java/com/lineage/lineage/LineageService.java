package com.lineage.lineage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineage.connect.KafkaConnectClient;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.HttpTransport;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Arrays;

public class LineageService {
    private final KafkaConnectClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final OpenLineage openLineage = new OpenLineage(URI.create("https://github.com/OpenLineage/OpenLineage"));
    private final OpenLineageClient olClient;
    private final String namespace = "kafka-connect";
    private final String lineageUrl = "http://marquez-api:4000/api/v1/lineage";

    public LineageService(KafkaConnectClient client) {
        this.client = client;
        this.olClient = new OpenLineageClient(
            HttpTransport.builder().uri(lineageUrl).build()
        );
    }

    public void pollAndEmit() throws Exception {
        var connectorsJson = client.getConnectors();
        List<String> connectors = mapper.readValue(connectorsJson,
                mapper.getTypeFactory().constructCollectionType(List.class, String.class));

        for (String name : connectors) {
            var configJson = client.getConfig(name);
            Map<String, Object> config = mapper.readValue(configJson, Map.class);

            var lineage = mapToLineage(name, config);
            emit(lineage);
        }
    }

    private LineageData mapToLineage(String name, Map<String, Object> config) {
        String topics = (String) config.getOrDefault("topics", "");
        String table = (String) config.getOrDefault("table.name.format", "");
        return new LineageData(name, topics, table);
    }

    private void emit(LineageData data) {
        try {
            // Extrai inputs (Kafka topics) e output (DB table)
            List<OpenLineage.InputDataset> inputDatasets = Arrays.stream(data.input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(topic -> openLineage.newInputDatasetBuilder()
                        .namespace(namespace)
                        .name(topic)
                        .build())
                .collect(Collectors.toList());

            List<OpenLineage.OutputDataset> outputDatasets = data.output == null || data.output.isEmpty() ? List.of() :
                List.of(openLineage.newOutputDatasetBuilder()
                        .namespace(namespace)
                        .name(data.output)
                        .build());

            OpenLineage.RunEvent runEvent = openLineage.newRunEventBuilder()
                .eventType(OpenLineage.RunEvent.EventType.START)
                .eventTime(OffsetDateTime.now().toZonedDateTime())
                .run(openLineage.newRunBuilder().runId(UUID.randomUUID()).build())
                .job(openLineage.newJobBuilder().namespace(namespace).name(data.jobName).build())
                .inputs(inputDatasets)
                .outputs(outputDatasets)
                .build();

            olClient.emit(runEvent);
            System.out.println("Emitted lineage to OpenLineage: " + data);
        } catch (Exception e) {
            System.err.println("Failed to emit lineage to OpenLineage: " + data + ", error: " + e.getMessage());
        }
    }
}
