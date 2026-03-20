package com.lineage;

import com.lineage.connect.KafkaConnectClient;
import com.lineage.lineage.LineageService;

public class Main {
    static void main(String[] args) throws Exception {
        var client = new KafkaConnectClient();
        var lineageService = new LineageService(client);

        while (true) {
            try {
                lineageService.pollAndEmit();
            } catch (Exception e) {
                System.err.println("Error during lineage polling: " + e.getMessage());
                e.printStackTrace();
            }
            Thread.sleep(30000);
        }
    }
}
