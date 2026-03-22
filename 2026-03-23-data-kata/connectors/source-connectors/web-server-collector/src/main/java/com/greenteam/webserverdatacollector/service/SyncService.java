package com.greenteam.webserverdatacollector.service;

import com.greenteam.webserverdatacollector.dto.Salesman;
import com.greenteam.webserverdatacollector.producer.SalesmanProducer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SyncService {

    private final RestClient restClient;
    private final SalesmanProducer salesmanProducer;
    private final OffsetService offsetService;

    public SyncService(RestClient.Builder builder, SalesmanProducer salesmanProducer, OffsetService offsetService) {
        this.restClient = builder.baseUrl("http://localhost:8089").build();
        this.salesmanProducer = salesmanProducer;
        this.offsetService = offsetService;
    }

    public void startSync() {
        try {

            Long latestOffset = offsetService.getOffset("webserver-api");

            Long startId = latestOffset + 1; // move to the next item
            Salesman[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sales")
                            .queryParam("startId", startId)
                            .queryParam("items", 2L)
                            .build())
                    .retrieve()
                    .body(Salesman[].class);

            System.out.println("=== Salesman Received ===");

            Long currentOffset = startId;
            for (Salesman sale : response) {
                salesmanProducer.send(sale);
                offsetService.updateOffset("webserver-api", currentOffset);
                currentOffset++;
                System.out.println("Sent to Kafka: " + sale);
            }
        } catch (Exception e) {
            System.err.println("Failed to calll the API: " + e.getMessage());
        }
    }
}