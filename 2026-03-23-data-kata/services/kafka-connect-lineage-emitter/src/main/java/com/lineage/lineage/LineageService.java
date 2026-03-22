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
    private final String namespace = "green-team-data-kata";
    private final String datasetNamespace = "green-team-data-kata";
    private final String lineageUrl = System.getenv("MARQUEZ_API_URL") != null ? System.getenv("MARQUEZ_API_URL") : "http://localhost:4000/api/v1/lineage";

    public LineageService(KafkaConnectClient client) {
        this.client = client;
        this.olClient = new OpenLineageClient(
            HttpTransport.builder().uri(lineageUrl).build()
        );
    }

    private LineageData mapToLineage(String name, Map<String, Object> config) {
        String topics = (String) config.getOrDefault("topics", "");
        String table = (String) config.getOrDefault("table.name.format", "");
        return new LineageData(name, topics, table);
    }

    /**
     * Returns the schema (list of fields) for a given topic and connector type (sink/source).
     * To add new schemas, add a new case for the connector type and topic.
     */
    private List<OpenLineage.SchemaDatasetFacetFields> getSchemaFieldsFor(String connectorType, String value) {
        switch (connectorType.toLowerCase()) {
            case "sink":
                switch (value) {
                    case "total_sales_by_city_window_deltas":
                        // Windowed reporting table for city
                        return List.of(
                                openLineage.newSchemaDatasetFacetFields("city_name", "TEXT", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("sale_date", "DATE", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("total_amount", "NUMERIC(18,2)", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("total_units", "BIGINT", null, null, null)
                        );
                    case "top_salesman_window_deltas":
                        // Windowed reporting table for salesman
                        return List.of(
                                openLineage.newSchemaDatasetFacetFields("salesman_id", "INTEGER", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("salesman_name", "TEXT", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("sale_date", "DATE", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("total_amount", "NUMERIC(18,2)", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("total_units", "BIGINT", null, null, null)
                        );
                    default:
                        return List.of();
                }
            case "source":
                switch (value) {
                    case "top-sales":
                        // Schema for top-sales (source)
                        return List.of(
                            openLineage.newSchemaDatasetFacetFields("city_name", "STRING", "Aggregation key", null, null),
                            openLineage.newSchemaDatasetFacetFields("sale_date", "DATE", "Aggregation key (day)", null, null),
                            openLineage.newSchemaDatasetFacetFields("total_amount", "DECIMAL(2)", "Sum of quantity × amount", null, null),
                            openLineage.newSchemaDatasetFacetFields("total_units", "INTEGER", "Sum of quantity", null, null)
                        );
                    case "top-salesman":
                        // Schema for top-salesman (source)
                        return List.of(
                            openLineage.newSchemaDatasetFacetFields("salesman_id", "INTEGER", "Reliable salesman key", null, null),
                            openLineage.newSchemaDatasetFacetFields("salesman_name", "STRING", "Winning salesman for the day", null, null),
                            openLineage.newSchemaDatasetFacetFields("sale_date", "DATE", "Sales day used for the ranking", null, null),
                            openLineage.newSchemaDatasetFacetFields("total_amount", "DECIMAL(2)", "Sum of quantity × amount", null, null),
                            openLineage.newSchemaDatasetFacetFields("total_units", "INTEGER", "Sum of units", null, null)
                        );
                    // Add more source topics here
                    default:
                        return List.of();
                }
            default:
                return List.of();
        }
    }

    /**
     * Builds the schema facet for a given topic and connector type (sink/source).
     */
    private OpenLineage.DatasetFacets buildSchemaFacetFor(String connectorType, String value) {
        List<OpenLineage.SchemaDatasetFacetFields> fields = getSchemaFieldsFor(connectorType, value);
        if (!fields.isEmpty()) {
            return openLineage.newDatasetFacetsBuilder()
                .schema(openLineage.newSchemaDatasetFacetBuilder().fields(fields).build())
                .build();
        }
        return openLineage.newDatasetFacetsBuilder().build();
    }

    private void emit(LineageData data) {
        try {
            // For each input/output, try to get the schema by topic and connector type
            List<OpenLineage.InputDataset> inputDatasets = Arrays.stream(data.input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(topic -> openLineage.newInputDatasetBuilder()
                        .namespace(datasetNamespace)
                        .name(topic)
                        .facets(buildSchemaFacetFor("source", topic))
                        .build())
                .collect(Collectors.toList());

            List<OpenLineage.OutputDataset> outputDatasets = data.output == null || data.output.isEmpty() ? List.of() :
                List.of(openLineage.newOutputDatasetBuilder()
                        .namespace(datasetNamespace)
                        .name(data.output)
                        .facets(buildSchemaFacetFor("sink", data.output))
                        .build());

            UUID runId = UUID.randomUUID();
            var jobBuilder = openLineage.newJobBuilder().namespace(namespace).name(data.jobName);

            OpenLineage.RunEvent startEvent = openLineage.newRunEventBuilder()
                .eventType(OpenLineage.RunEvent.EventType.START)
                .eventTime(OffsetDateTime.now().toZonedDateTime())
                .run(openLineage.newRunBuilder().runId(runId).build())
                .job(jobBuilder.build())
                .inputs(inputDatasets)
                .outputs(outputDatasets)
                .build();
            olClient.emit(startEvent);

            OpenLineage.RunEvent completeEvent = openLineage.newRunEventBuilder()
                .eventType(OpenLineage.RunEvent.EventType.COMPLETE)
                .eventTime(OffsetDateTime.now().toZonedDateTime())
                .run(openLineage.newRunBuilder().runId(runId).build())
                .job(jobBuilder.build())
                .inputs(inputDatasets)
                .outputs(outputDatasets)
                .build();
            olClient.emit(completeEvent);

            System.out.println("Emitted lineage START and COMPLETE to OpenLineage: " + data);
        } catch (Exception e) {
            System.err.println("Failed to emit lineage to OpenLineage: " + data + ", error: " + e.getMessage());
        }
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
}
