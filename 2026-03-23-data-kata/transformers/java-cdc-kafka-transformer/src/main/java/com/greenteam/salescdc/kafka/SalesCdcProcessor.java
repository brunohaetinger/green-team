package com.greenteam.salescdc.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.salescdc.model.DebeziumMessage;
import com.greenteam.salescdc.model.SalesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class SalesCdcProcessor {

    private static final Logger log = LoggerFactory.getLogger(SalesCdcProcessor.class);

    private final SalesEventPublisher publisher;
    private final ObjectMapper objectMapper;

    public SalesCdcProcessor(SalesEventPublisher publisher, ObjectMapper objectMapper) {
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topics.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message, Acknowledgment ack) {
        try {
            DebeziumMessage debeziumMessage = objectMapper.readValue(message, DebeziumMessage.class);
            process(debeziumMessage);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }

    private void process(DebeziumMessage debeziumMessage) {
        if (debeziumMessage.payload() == null || debeziumMessage.payload().after() == null) {
            log.warn("Received message with null payload or after, skipping.");
            return;
        }

        DebeziumMessage.RecordData record = debeziumMessage.payload().after();
        publisher.publish(new SalesData(record.id(), record.salesmanId(), record.productId(), record.quantity()));
    }
}
