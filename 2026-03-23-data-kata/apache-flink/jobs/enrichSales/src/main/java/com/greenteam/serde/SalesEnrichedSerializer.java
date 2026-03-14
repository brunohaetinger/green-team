package com.greenteam.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greenteam.config.JobConfig;
import com.greenteam.model.SalesEnrichedEvent;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

public class SalesEnrichedSerializer implements KafkaRecordSerializationSchema<SalesEnrichedEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ProducerRecord<byte[], byte[]> serialize(
        SalesEnrichedEvent element,
        KafkaSinkContext context,
        Long timestamp
    ) {
        byte[] key = Integer.toString(element.saleId).getBytes(StandardCharsets.UTF_8);
        byte[] value = buildJson(element).getBytes(StandardCharsets.UTF_8);
        return new ProducerRecord<>(JobConfig.OUTPUT_TOPIC, key, value);
    }

    private String buildJson(SalesEnrichedEvent element) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("salesman_id", element.salesmanId);
        root.put("salesman_name", element.salesmanName);
        root.put("sale_id", element.saleId);
        root.put("quantity", element.quantity);
        root.put("product_id", element.productId);
        root.put("store_id", element.storeId);
        root.put("city_name", element.cityName);
        root.put("store_name", element.storeName);
        root.put("sale_date", element.saleDate);
        root.put("country_name", element.countryName);
        root.put("amount", element.amount.stripTrailingZeros().toPlainString());
        return root.toString();
    }
}