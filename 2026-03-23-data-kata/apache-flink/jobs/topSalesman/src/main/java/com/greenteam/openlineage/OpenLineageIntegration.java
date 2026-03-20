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
import java.util.Collections;
import java.util.UUID;

public class OpenLineageIntegration {
    /**
     * Enum to represent the type of Kafka dataset (input or output).
     */
    private enum KafkaDatasetType {
        SALES_ENRICHED, TOP_SALESMAN
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

    public void emitKafkaToKafkaEvent(
            String inputTopic,
            String outputTopic,
            OpenLineage.RunEvent.EventType eventType
    ) {
        logger.info("Emitting OpenLineage {} event for run ID: {}", eventType, jobId);
        OpenLineage.JobTypeJobFacet jobTypeFacet = ol.newJobTypeJobFacet("STREAMING", "FLINK", "CUSTOM_FLINK_JOB");

        OpenLineage.InputDataset inputDataset = (OpenLineage.InputDataset) buildKafkaDataset(inputTopic, KafkaDatasetType.SALES_ENRICHED);
        OpenLineage.OutputDataset outputDataset = (OpenLineage.OutputDataset) buildKafkaDataset(outputTopic, KafkaDatasetType.TOP_SALESMAN);

        OpenLineage.RunEvent event = ol.newRunEventBuilder()
                .eventTime(ZonedDateTime.now())
                .eventType(eventType)
                .run(ol.newRunBuilder().runId(UUID.fromString(jobId)).build())
                .job(ol.newJobBuilder()
                        .namespace(jobNamespace)
                        .name(jobName)
                        .facets(ol.newJobFacetsBuilder().jobType(jobTypeFacet).build())
                        .build())
                .inputs(Collections.singletonList(inputDataset))
                .outputs(Collections.singletonList(outputDataset))
                .build();

        try {
            ObjectMapper mapper = new ObjectMapper();
            logger.info("OpenLineage event JSON: {}", mapper.writeValueAsString(event));
        } catch (Exception e) {
            logger.warn("Failed to serialize OpenLineage event to JSON", e);
        }
        transport.emit(event);
    }

    /**
     * Overloaded method to build a Kafka dataset using the KafkaDatasetType enum.
     */
    private OpenLineage.Dataset buildKafkaDataset(String topic, KafkaDatasetType type) {
        OpenLineage.DatasetFacetsBuilder facetsBuilder = ol.newDatasetFacetsBuilder()
                .dataSource(ol.newDatasourceDatasetFacet("kafka", URI.create(kafkaNamespace)))
                .schema(ol.newSchemaDatasetFacet(getSchemaFields(type)));

        return switch (type) {
            case SALES_ENRICHED -> ol.newInputDatasetBuilder()
                    .namespace(jobNamespace)
                    .name(topic)
                    .facets(facetsBuilder.build())
                    .build();
            case TOP_SALESMAN -> ol.newOutputDatasetBuilder()
                    .namespace(jobNamespace)
                    .name(topic)
                    .facets(facetsBuilder.build())
                    .build();
        };
    }

    /**
     * Returns the schema fields for the given dataset type.
     */
    private java.util.List<OpenLineage.SchemaDatasetFacetFields> getSchemaFields(KafkaDatasetType type) {
        return switch (type) {
            case SALES_ENRICHED -> java.util.List.of(
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
            case TOP_SALESMAN -> java.util.List.of(
                    ol.newSchemaDatasetFacetFields("salesman_id", "INT", null, 1L, null),
                    ol.newSchemaDatasetFacetFields("salesman_name", "STRING", null, 2L, null),
                    ol.newSchemaDatasetFacetFields("sale_date", "DATE", null, 3L, null),
                    ol.newSchemaDatasetFacetFields("total_amount", "DECIMAL", null, 4L, null),
                    ol.newSchemaDatasetFacetFields("total_units", "INT", null, 5L, null)
            );
        };
    }

    public void close() throws IOException {
        transport.close();
    }
}
