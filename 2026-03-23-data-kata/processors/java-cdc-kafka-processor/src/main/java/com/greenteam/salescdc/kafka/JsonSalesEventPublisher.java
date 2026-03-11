package com.greenteam.salescdc.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.salescdc.model.SalesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class JsonSalesEventPublisher implements SalesEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(JsonSalesEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String outputTopic;

    public JsonSalesEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${kafka.topics.output}") String outputTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.outputTopic = outputTopic;
    }

    @Override
    public void publish(SalesData salesData) {
        try {
            String payload = objectMapper.writeValueAsString(salesData);
            kafkaTemplate.send(outputTopic, String.valueOf(salesData.id()), payload);
            log.info("Published JSON to {}: {}", outputTopic, payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish JSON event", e);
        }
    }
}
