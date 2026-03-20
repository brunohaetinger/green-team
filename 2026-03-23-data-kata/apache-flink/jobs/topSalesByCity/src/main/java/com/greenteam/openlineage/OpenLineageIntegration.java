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

        OpenLineage.InputDataset inputDataset = (OpenLineage.InputDataset) buildKafkaDataset(inputTopic, true);
        OpenLineage.OutputDataset outputDataset = (OpenLineage.OutputDataset) buildKafkaDataset(outputTopic, false);

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
        // Log do JSON do evento
        try {
            ObjectMapper mapper = new ObjectMapper();
            logger.info("OpenLineage event JSON: {}", mapper.writeValueAsString(event));
        } catch (Exception e) {
            logger.warn("Failed to serialize OpenLineage event to JSON", e);
        }
        transport.emit(event);
    }

    private OpenLineage.Dataset buildKafkaDataset(String topic, boolean isInput) {
        // Adiciona o facet de schema conforme o tipo do dataset
        OpenLineage.DatasetFacetsBuilder facetsBuilder = ol.newDatasetFacetsBuilder()
                .dataSource(ol.newDatasourceDatasetFacet("kafka", URI.create(kafkaNamespace)));

        // Schema facet
        if (isInput) {
            // SaleEvent schema
            facetsBuilder.schema(
                    ol.newSchemaDatasetFacet(
                            java.util.List.of(
                                    ol.newSchemaDatasetFacetFields("cityName", "STRING", null, 1L, null),
                                    ol.newSchemaDatasetFacetFields("saleDate", "DATE", null, 2L, null),
                                    ol.newSchemaDatasetFacetFields("quantity", "INT", null, 3L, null),
                                    ol.newSchemaDatasetFacetFields("amount", "DECIMAL", null, 4L, null)
                            )
                    )
            );
            return ol.newInputDatasetBuilder()
                    .namespace(jobNamespace)
                    .name(topic)
                    .facets(facetsBuilder.build())
                    .build();
        } else {
            // CitySalesResult schema
            facetsBuilder.schema(
                    ol.newSchemaDatasetFacet(
                            java.util.List.of(
                                    ol.newSchemaDatasetFacetFields("cityName", "STRING", null, 1L, null),
                                    ol.newSchemaDatasetFacetFields("saleDate", "DATE", null, 2L, null),
                                    ol.newSchemaDatasetFacetFields("totalAmount", "DECIMAL", null, 3L, null),
                                    ol.newSchemaDatasetFacetFields("totalUnits", "LONG", null, 4L, null),
                                    ol.newSchemaDatasetFacetFields("processedAt", "TIMESTAMP", null, 5L, null),
                                    ol.newSchemaDatasetFacetFields("windowStart", "LONG", null, 6L, null),
                                    ol.newSchemaDatasetFacetFields("windowEnd", "LONG", null, 7L, null)
                            )
                    )
            );
            return ol.newOutputDatasetBuilder()
                    .namespace(jobNamespace)
                    .name(topic)
                    .facets(facetsBuilder.build())
                    .build();
        }
    }

    public void close() throws IOException {
        transport.close();
    }
}
