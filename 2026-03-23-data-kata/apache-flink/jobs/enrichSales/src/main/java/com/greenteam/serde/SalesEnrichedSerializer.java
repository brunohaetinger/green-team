package com.greenteam.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greenteam.config.JobConfig;
import com.greenteam.model.SalesEnrichedEvent;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.nio.charset.StandardCharsets;

/*
 * This class is responsible for serializing the SalesEnrichedEvent objects into JSON format and creating ProducerRecord objects that can be sent to the output topic in Kafka.
 * It uses the Jackson library to convert the SalesEnrichedEvent objects into JSON strings, and then creates ProducerRecord objects with the appropriate key and value for each enriched sale event.
 */
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
        root.put("event_id", element.eventId);
        root.put("trace_id", element.traceId);
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