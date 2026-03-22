package com.greenteam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.SalesEvent;
import com.greenteam.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SalesEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SalesEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final S3Service s3Service;

    public SalesEventConsumer(S3Service s3Service) {
        this.s3Service = s3Service;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "sales-enriched", groupId = "audit-service-group")
    public void consume(String message) {
        logger.info("Received sales-enriched event: {}", message);

        try {
            SalesEvent event = objectMapper.readValue(message, SalesEvent.class);
            s3Service.saveEvent(event);
            logger.info("Successfully saved event for store: {}", event.getStoreName());
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse sales event: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to process sales event: {}", e.getMessage(), e);
        }
    }
}
