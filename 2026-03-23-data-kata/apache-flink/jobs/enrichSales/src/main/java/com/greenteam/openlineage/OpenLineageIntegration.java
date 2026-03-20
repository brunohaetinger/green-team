package com.greenteam.openlineage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.config.JobConfig;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.transports.HttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OpenLineageIntegration {
    /**
     * Enum to represent the type of Kafka dataset (input or output).
     */
    private enum KafkaDatasetType {
        SALES_ENRICHED, SALES, SALESMANS, STORES
    }
    private static final Logger logger = LoggerFactory.getLogger(OpenLineageIntegration.class);

    private final OpenLineage ol;
    private final HttpTransport transport;
    private final String jobName;
    private final String jobNamespace;
    private final String kafkaNamespace;
    private final String jobId;

    public OpenLineageIntegration(String jobId) {
        this.jobName = JobConfig.JOB_NAME;
        this.jobNamespace = JobConfig.JOB_NAMESPACE;
        this.kafkaNamespace = "kafka://" + JobConfig.BOOTSTRAP_SERVERS;
        this.ol = new OpenLineage(URI.create("https://github.com/OpenLineage/OpenLineage"));
        this.transport = HttpTransport.builder().uri(URI.create(JobConfig.OPEN_LINEAGE_URL)).build();
        this.jobId = jobId;
    }

    public void emitKafkaToKafkaEvent(OpenLineage.RunEvent.EventType eventType) {
        logger.info("Emitting OpenLineage {} event for run ID: {}", eventType, jobId);
        OpenLineage.JobTypeJobFacet jobTypeFacet = ol.newJobTypeJobFacet("STREAMING", "FLINK", "CUSTOM_FLINK_JOB");

        java.util.List<OpenLineage.InputDataset> inputDatasets = new java.util.ArrayList<>();
        for (String topic : List.of(JobConfig.SALES_TOPIC, JobConfig.SALESMANS_TOPIC, JobConfig.STORES_TOPIC)) {
            inputDatasets.add((OpenLineage.InputDataset) buildKafkaDatasetInput(topic));
        }
        java.util.List<OpenLineage.OutputDataset> outputDatasets = new java.util.ArrayList<>();
        for (String topic : List.of(JobConfig.OUTPUT_TOPIC, "sales-expired")) {
            outputDatasets.add((OpenLineage.OutputDataset) buildKafkaDatasetOutput(topic));
        }

        OpenLineage.RunEvent event = ol.newRunEventBuilder()
                .eventTime(ZonedDateTime.now())
                .eventType(eventType)
                .run(ol.newRunBuilder().runId(UUID.fromString(jobId)).build())
                .job(ol.newJobBuilder()
                        .namespace(jobNamespace)
                        .name(jobName)
                        .facets(ol.newJobFacetsBuilder().jobType(jobTypeFacet).build())
                        .build())
                .inputs(inputDatasets)
                .outputs(outputDatasets)
                .build();

        try {
            ObjectMapper mapper = new ObjectMapper();
            logger.info("OpenLineage event JSON: {}", mapper.writeValueAsString(event));
        } catch (Exception e) {
            logger.warn("Failed to serialize OpenLineage event to JSON", e);
        }
        transport.emit(event);
    }

    private OpenLineage.Dataset buildKafkaDatasetInput(String topic) {
        OpenLineage.DatasetFacetsBuilder facetsBuilder = ol.newDatasetFacetsBuilder()
                .dataSource(ol.newDatasourceDatasetFacet("kafka", URI.create(kafkaNamespace)))
                .schema(ol.newSchemaDatasetFacet(getInputSchemaFields(topic)));

        return ol.newInputDatasetBuilder()
                .namespace(jobNamespace)
                .name(topic)
                .facets(facetsBuilder.build())
                .build();
    }

    private OpenLineage.Dataset buildKafkaDatasetOutput(String topic) {
        OpenLineage.DatasetFacetsBuilder facetsBuilder = ol.newDatasetFacetsBuilder()
                .dataSource(ol.newDatasourceDatasetFacet("kafka", URI.create(kafkaNamespace)))
                .schema(ol.newSchemaDatasetFacet(getOutputSchemaFields(topic)));

        return ol.newOutputDatasetBuilder()
                .namespace(jobNamespace)
                .name(topic)
                .facets(facetsBuilder.build())
                .build();
    }

    /**
     * Returns the schema fields for the given dataset type.
     */
    private List<OpenLineage.SchemaDatasetFacetFields> getOutputSchemaFields(String topic) {
        if (topic.equals(JobConfig.OUTPUT_TOPIC)) {
            return java.util.List.of(
                    ol.newSchemaDatasetFacetFields("salesman_id", "INT", null, 1L, null),
                    ol.newSchemaDatasetFacetFields("salesman_name", "STRING", null, 2L, null),
                    ol.newSchemaDatasetFacetFields("sale_id", "INT", null, 3L, null),
                    ol.newSchemaDatasetFacetFields("quantity", "INT", null, 4L, null),
                    ol.newSchemaDatasetFacetFields("product_id", "INT", null, 5L, null),
                    ol.newSchemaDatasetFacetFields("store_id", "INT", null, 6L, null),
                    ol.newSchemaDatasetFacetFields("city_name", "STRING", null, 7L, null),
                    ol.newSchemaDatasetFacetFields("store_name", "STRING", null, 8L, null),
                    ol.newSchemaDatasetFacetFields("sale_date", "STRING", null, 9L, null),
                    ol.newSchemaDatasetFacetFields("country_name", "STRING", null, 10L, null),
                    ol.newSchemaDatasetFacetFields("amount", "DECIMAL", null, 11L, null)
            );
        } else if (topic.equals(JobConfig.EXPIRED_TOPIC)) {
            return java.util.List.of(
                    ol.newSchemaDatasetFacetFields("sale_id", "INT", null, 1L, null),
                    ol.newSchemaDatasetFacetFields("salesman_id", "INT", null, 2L, null),
                    ol.newSchemaDatasetFacetFields("salesman_name", "STRING", null, 3L, null),
                    ol.newSchemaDatasetFacetFields("sale_date", "STRING", null, 4L, null),
                    ol.newSchemaDatasetFacetFields("product_id", "INT", null, 5L, null),
                    ol.newSchemaDatasetFacetFields("store_id", "INT", null, 6L, null),
                    ol.newSchemaDatasetFacetFields("city_name", "STRING", null, 7L, null),
                    ol.newSchemaDatasetFacetFields("store_name", "STRING", null, 8L, null),
                    ol.newSchemaDatasetFacetFields("country_name", "STRING", null, 9L, null),
                    ol.newSchemaDatasetFacetFields("amount", "DECIMAL", null, 10L, null),
                    ol.newSchemaDatasetFacetFields("quantity", "INT", null, 11L, null),
                    ol.newSchemaDatasetFacetFields("expires_at", "LONG", null, 12L, null),
                    ol.newSchemaDatasetFacetFields("reason", "STRING", null, 13L, null)
            );
        } else {
            return java.util.List.of();
        }
    }

    /**
     * Returns the schema fields for the given dataset type.
     */
    private List<OpenLineage.SchemaDatasetFacetFields> getInputSchemaFields(String topicName) {
        return switch (topicName) {
            case JobConfig.SALES_TOPIC -> List.of(
                    ol.newSchemaDatasetFacetFields("id", "INT", null, 1L, null),
                    ol.newSchemaDatasetFacetFields("salesman_id", "INT", null, 2L, null),
                    ol.newSchemaDatasetFacetFields("store_id", "INT", null, 3L, null),
                    ol.newSchemaDatasetFacetFields("amount", "DECIMAL", null, 4L, null),
                    ol.newSchemaDatasetFacetFields("sale_date", "STRING", null, 5L, null),
                    ol.newSchemaDatasetFacetFields("product_id", "INT", null, 6L, null),
                    ol.newSchemaDatasetFacetFields("quantity", "INT", null, 7L, null)
            );
            case JobConfig.SALESMANS_TOPIC -> List.of(
                    ol.newSchemaDatasetFacetFields("id", "INT", null, 1L, null),
                    ol.newSchemaDatasetFacetFields("name", "STRING", null, 2L, null),
                    ol.newSchemaDatasetFacetFields("store_id", "INT", null, 3L, null)
            );
            case JobConfig.STORES_TOPIC -> List.of(
                    ol.newSchemaDatasetFacetFields("id", "INT", null, 1L, null),
                    ol.newSchemaDatasetFacetFields("name", "STRING", null, 2L, null),
                    ol.newSchemaDatasetFacetFields("city", "STRING", null, 3L, null),
                    ol.newSchemaDatasetFacetFields("state", "STRING", null, 4L, null),
                    ol.newSchemaDatasetFacetFields("country", "STRING", null, 5L, null)
            );
            default -> java.util.List.of();
        };
    }

    public void close() throws IOException {
        transport.close();
    }
}
