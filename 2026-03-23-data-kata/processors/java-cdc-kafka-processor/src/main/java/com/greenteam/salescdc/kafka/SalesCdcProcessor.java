package com.greenteam.salescdc.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.salescdc.model.DebeziumMessage;
import com.greenteam.salescdc.model.SalesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SalesCdcProcessor {

    private static final Logger log = LoggerFactory.getLogger(SalesCdcProcessor.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String outputTopic;

    public SalesCdcProcessor(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${kafka.topics.output}") String outputTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.outputTopic = outputTopic;
    }

    @KafkaListener(topics = "${kafka.topics.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            DebeziumMessage debeziumMessage = objectMapper.readValue(message, DebeziumMessage.class);
            process(debeziumMessage);
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }

    private void process(DebeziumMessage debeziumMessage) throws Exception {
        if (debeziumMessage.payload() == null || debeziumMessage.payload().after() == null) {
            log.warn("Received message with null payload or after, skipping.");
            return;
        }

        DebeziumMessage.RecordData record = debeziumMessage.payload().after();
        SalesData salesData = new SalesData(record.id(), record.name());
        String payload = objectMapper.writeValueAsString(salesData);

        kafkaTemplate.send(outputTopic, String.valueOf(salesData.id()), payload);
        log.info("Published to {}: {}", outputTopic, payload);
    }
}
