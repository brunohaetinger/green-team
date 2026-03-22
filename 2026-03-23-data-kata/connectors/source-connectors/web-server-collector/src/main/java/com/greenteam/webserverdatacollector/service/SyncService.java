package com.greenteam.webserverdatacollector.service;

import com.greenteam.webserverdatacollector.dto.Salesman;
import com.greenteam.webserverdatacollector.lineage.LineageService;
import com.greenteam.webserverdatacollector.producer.SalesmanProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    private final RestClient restClient;
    private final SalesmanProducer salesmanProducer;
    private final OffsetService offsetService;
    private final LineageService lineageService;

    @Value("${scheduler.batch-size}")
    private Long batchSize;

    public SyncService(RestClient.Builder builder, SalesmanProducer salesmanProducer, OffsetService offsetService,
                       LineageService lineageService) {
        this.restClient = builder.baseUrl("http://localhost:8089").build();
        this.salesmanProducer = salesmanProducer;
        this.offsetService = offsetService;
        this.lineageService = lineageService;
    }

    public void startSync() {
        logger.info("Starting sync");
        UUID runId = lineageService.startRun();
        try {
            Long latestOffset = offsetService.getOffset("webserver-api");

            Long startId = latestOffset + 1; // move to the next item
            Salesman[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sales")
                            .queryParam("startId", startId)
                            .queryParam("items", batchSize)
                            .build())
                    .retrieve()
                    .body(Salesman[].class);

            if(response == null) {
                // skip by design, nothing to process
                return;
            }

            logger.info("Fetched {} items", response.length);

            Long currentOffset = startId;
            for (Salesman sale : response) {
                salesmanProducer.send(sale);
                offsetService.updateOffset("webserver-api", currentOffset);
                currentOffset++;
                logger.info("Salesman (id={}) event published", sale.id());
            }

            lineageService.completeRun(runId);
        } catch (Exception e) {
            logger.error("Failed to process the scheduler", e);
        }
    }
}