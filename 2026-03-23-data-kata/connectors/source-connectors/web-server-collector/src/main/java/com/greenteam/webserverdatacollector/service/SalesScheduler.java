package com.greenteam.webserverdatacollector.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SalesScheduler {

    private final SyncService syncService;

    public SalesScheduler(SyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedRate = 10_000)
    public void fetchSalesman() {
        this.syncService.startSync();
    }
}
