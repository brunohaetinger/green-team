package com.greenteam.webserverdatacollector.producer;

import com.greenteam.webserverdatacollector.dto.Salesman;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SalesmanProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SalesmanProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(Salesman salesman) {
        String message = objectMapper.writeValueAsString(salesman);
        kafkaTemplate.send("salesmans", String.valueOf(salesman.id()), message);
    }
}