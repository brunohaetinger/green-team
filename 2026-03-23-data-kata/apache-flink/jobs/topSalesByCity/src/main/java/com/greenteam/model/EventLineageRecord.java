package com.greenteam.model;

public record EventLineageRecord(
        String eventId,
        String traceId,
        int saleId,
        String jobName,
        String stage,
        String inputTopic,
        String outputTopic,
        String aggregationKey,
        long processedAt
) {
}