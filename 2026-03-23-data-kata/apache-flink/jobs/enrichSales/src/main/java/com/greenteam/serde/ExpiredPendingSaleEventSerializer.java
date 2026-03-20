package com.greenteam.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greenteam.config.JobConfig;
import com.greenteam.model.ExpiredPendingSaleEvent;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

/**
 * Serializes ExpiredPendingSaleEvent to JSON and creates ProducerRecord for Kafka.
 */
public class ExpiredPendingSaleEventSerializer implements KafkaRecordSerializationSchema<ExpiredPendingSaleEvent> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ProducerRecord<byte[], byte[]> serialize(
            ExpiredPendingSaleEvent element,
            KafkaSinkContext context,
            Long timestamp
    ) {
        byte[] key = Integer.toString(element.saleId).getBytes(StandardCharsets.UTF_8);
        byte[] value = buildJson(element).getBytes(StandardCharsets.UTF_8);
        return new ProducerRecord<>(JobConfig.EXPIRED_TOPIC, key, value);
    }

    private String buildJson(ExpiredPendingSaleEvent element) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("sale_id", element.saleId);
        root.put("salesman_id", element.salesmanId);
        root.put("sale_date", element.saleDate);
        root.put("product_id", element.productId);
        root.put("store_id", element.storeId);
        root.put("city_name", element.cityName);
        root.put("store_name", element.storeName);
        root.put("country_name", element.countryName);
        root.put("amount", element.amount != null ? element.amount.stripTrailingZeros().toPlainString() : null);
        root.put("quantity", element.quantity);
        root.put("expires_at", element.expiresAt);
        root.put("reason", element.reason.name());
        return root.toString();
    }
}
