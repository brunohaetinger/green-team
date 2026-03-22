package com.greenteam.webserverdatacollector.lineage;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.HttpTransport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LineageService {

    public static final String OPEN_LINEAGE_NAMESPACE = "green-team-data-kata";
    public static final String JOB_NAME = "web-server-collector";

    private final OpenLineage openLineage = new OpenLineage(URI.create("https://github.com/OpenLineage/OpenLineage"));
    private final OpenLineageClient client;

    public LineageService(@Value("${openlineage.url}") String openlineageApi) {
        this.client = new OpenLineageClient(
                HttpTransport.builder()
                        .uri(openlineageApi + "/api/v1/lineage")
                        .build()
        );
    }

    public UUID startRun() {

        UUID runId = UUID.randomUUID();

        var job = openLineage.newJobBuilder()
                .namespace(OPEN_LINEAGE_NAMESPACE)
                .name(JOB_NAME)
                .build();


        var input = openLineage.newInputDatasetBuilder()
                .namespace("api")
                .name("salesman-webserver")
                .facets(openLineage.newDatasetFacetsBuilder()
                    .schema(openLineage.newSchemaDatasetFacetBuilder()
                        .fields(List.of(
                            openLineage.newSchemaDatasetFacetFields("id", "LONG", null, null, null),
                            openLineage.newSchemaDatasetFacetFields("name", "STRING", null, null, null),
                            openLineage.newSchemaDatasetFacetFields("store_id", "LONG", null, null, null)
                        )).build()
                    ).build()
                ).build();

        var output = openLineage.newOutputDatasetBuilder()
                .namespace("kafka")
                .name("salesmans")
                .facets(openLineage.newDatasetFacetsBuilder()
                    .schema(openLineage.newSchemaDatasetFacetBuilder()
                            .fields(List.of(
                                openLineage.newSchemaDatasetFacetFields("id", "LONG", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("name", "STRING", null, null, null),
                                openLineage.newSchemaDatasetFacetFields("store_id", "LONG", null, null, null)
                            )).build()
                    ).build()
                ).build();

        var event = openLineage.newRunEventBuilder()
                .eventType(OpenLineage.RunEvent.EventType.START)
                .eventTime(OffsetDateTime.now().toZonedDateTime())
                .run(openLineage.newRunBuilder().runId(runId).build())
                .job(job)
                .inputs(List.of(input))
                .outputs(List.of(output))
                .build();

        client.emit(event);

        return runId;
    }

    public void completeRun(UUID runId) {

        var job = openLineage.newJobBuilder()
                .namespace(OPEN_LINEAGE_NAMESPACE)
                .name(JOB_NAME)
                .build();

        var event = openLineage.newRunEventBuilder()
                .eventType(OpenLineage.RunEvent.EventType.COMPLETE)
                .eventTime(OffsetDateTime.now().toZonedDateTime())
                .run(openLineage.newRunBuilder().runId(runId).build())
                .job(job)
                .build();

        client.emit(event);
    }
}
