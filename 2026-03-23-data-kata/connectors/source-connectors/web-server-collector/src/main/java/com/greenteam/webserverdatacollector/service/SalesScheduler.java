package com.greenteam.webserverdatacollector.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

@Component
public class SalesScheduler {

    private final RestClient restClient;

    public SalesScheduler(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://localhost:8089").build();
    }

    @Scheduled(fixedRate = 10_000)
    public void fetchSales() {
        try {

            Long startId = 0L;
            Sale[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sales")
                            .queryParam("startId", startId)
                            .build())
                    .retrieve()
                    .body(Sale[].class);

            System.out.println("=== SALES RECEIVED ===");

            if (response != null) {
                Arrays.stream(response)
                        .forEach(System.out::println);
            } else {
                System.out.println("No data received");
            }

        } catch (Exception e) {
            System.err.println("Failed to calll the API: " + e.getMessage());
        }
    }
}
