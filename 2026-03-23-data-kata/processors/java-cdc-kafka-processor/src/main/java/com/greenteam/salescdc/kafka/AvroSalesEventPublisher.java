package com.greenteam.salescdc.kafka;

import com.greenteam.salescdc.model.SalesData;
import jakarta.annotation.PostConstruct;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Profile("avro")
public class AvroSalesEventPublisher implements SalesEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AvroSalesEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String outputTopic;
    private Schema schema;

    public AvroSalesEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topics.output}") String outputTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.outputTopic = outputTopic;
    }

    @PostConstruct
    void loadSchema() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/avro/sales-event.avsc")) {
            schema = new Schema.Parser().parse(is);
        }
    }

    @Override
    public void publish(SalesData salesData) {
        GenericRecord record = new GenericData.Record(schema);
        record.put("id",          salesData.id());
        record.put("salesman_id", salesData.salesmanId());
        record.put("product_id",  salesData.productId());
        record.put("quantity",    salesData.quantity());

        kafkaTemplate.send(outputTopic, String.valueOf(salesData.id()), record);
        log.info("Published Avro to {}: id={}, salesman_id={}, product_id={}, quantity={}",
                outputTopic, salesData.id(), salesData.salesmanId(), salesData.productId(), salesData.quantity());
    }
}
