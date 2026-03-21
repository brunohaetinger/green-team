package com.greenteam.webserverdatacollector.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

@Component
public class SalesScheduler {

    private final RestClient restClient;
    private final SalesmanProducer salesmanProducer;
    private Long lastProcessedId = 1L;

    public SalesScheduler(RestClient.Builder builder, SalesmanProducer salesmanProducer) {
        this.restClient = builder.baseUrl("http://localhost:8089").build();
        this.salesmanProducer = salesmanProducer;
    }

    @Scheduled(fixedRate = 10_000)
    public void fetchSales() {
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
