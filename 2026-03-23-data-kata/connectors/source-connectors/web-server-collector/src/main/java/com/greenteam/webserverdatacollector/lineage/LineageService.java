package com.greenteam.webserverdatacollector.lineage;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.HttpTransport;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LineageService {

    private final OpenLineage openLineage = new OpenLineage(URI.create("https://github.com/OpenLineage/OpenLineage"));
    private final OpenLineageClient client;

    public LineageService() {
        this.client = new OpenLineageClient(
                HttpTransport.builder()
                        .uri("http://localhost:4000/api/v1/lineage")
                        .build()
        );
    }

    public UUID startRun() {

        UUID runId = UUID.randomUUID();

        var job = openLineage.newJobBuilder()
                .namespace("green-team-data-kata")
                .name("salesman-ingestion-job")
                .build();


        var input = openLineage.newInputDatasetBuilder()
                .namespace("api")
                .name("salesman-webserver")
                .facets(openLineage.newDatasetFacetsBuilder()
                    .dataSource(
                        openLineage.newDatasourceDatasetFacet(
                        "api",
                            URI.create("http://localhost:8089/sales")
                        )
                    )
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
                .namespace("green-team-data-kata")
                .name("web-server-collector")
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
