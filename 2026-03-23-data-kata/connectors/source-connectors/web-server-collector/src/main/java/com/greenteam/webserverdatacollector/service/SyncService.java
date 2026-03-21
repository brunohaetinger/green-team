package com.greenteam.webserverdatacollector.service;

import com.greenteam.webserverdatacollector.dto.Salesman;
import com.greenteam.webserverdatacollector.producer.SalesmanProducer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SyncService {

    private final RestClient restClient;
    private final SalesmanProducer salesmanProducer;
    private Long lastProcessedId = 1L;

    public SyncService(RestClient.Builder builder, SalesmanProducer salesmanProducer) {
        this.restClient = builder.baseUrl("http://localhost:8089").build();
        this.salesmanProducer = salesmanProducer;
    }

    public void startSync() {
        try {
            Long startId = 0L;
            Salesman[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sales")
                            .queryParam("startId", startId)
                            .build())
                    .retrieve()
                    .body(Salesman[].class);

            System.out.println("=== SALES RECEIVED ===");

            System.out.println("=== PUBLISHING SALES ===");

            long maxId = lastProcessedId;

            for (Salesman sale : response) {
                salesmanProducer.send(sale);
                System.out.println("Sent to Kafka: " + sale);

                if (sale.id() > maxId) {
                    maxId = sale.id();
                }
            }

            lastProcessedId = maxId + 1;
        } catch (Exception e) {
            System.err.println("Failed to calll the API: " + e.getMessage());
        }
    }
}