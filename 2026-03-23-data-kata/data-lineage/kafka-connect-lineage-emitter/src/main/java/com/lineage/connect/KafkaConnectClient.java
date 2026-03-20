package com.lineage.connect;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KafkaConnectClient {

    private final String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    public KafkaConnectClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getConnectors() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/connectors"))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String getConfig(String name) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/connectors/" + name + "/config"))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
