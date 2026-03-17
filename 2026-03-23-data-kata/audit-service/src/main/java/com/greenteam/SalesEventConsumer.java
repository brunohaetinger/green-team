package com.greenteam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SalesEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SalesEventConsumer.class);

    @KafkaListener(topics = "sales-enriched", groupId = "audit-service-group")
    public void consume(String message) {
        logger.info("Received sales-enriched event: {}", message);
    }
}
