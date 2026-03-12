package com.greenteam.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greenteam.config.JobConfig;
import com.greenteam.model.TopSalesmanResult;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class TopSalesmanResultSerializer implements KafkaRecordSerializationSchema<TopSalesmanResult> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Schema DECIMAL_SCHEMA = Decimal.schema(2);

    @Override
    public ProducerRecord<byte[], byte[]> serialize(
        TopSalesmanResult element,
        KafkaSinkContext context,
        Long timestamp
    ) {
        byte[] key = (element.salesmanId + "|" + element.windowEnd).getBytes(StandardCharsets.UTF_8);
        byte[] value = buildValue(element).getBytes(StandardCharsets.UTF_8);
        return new ProducerRecord<>(JobConfig.OUTPUT_TOPIC, key, value);
    }

    private static String buildValue(TopSalesmanResult element) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("schema", buildSchema());

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("schema_version", "1.0");
        payload.put("aggregation_type", "top_salesman");
        payload.put("salesman_id", element.salesmanId);

        if (element.countryId == null) {
            payload.putNull("country_id");
        } else {
            payload.put("country_id", element.countryId);
        }

        payload.put("window_start", toEpochMillis(element.windowStart));
        payload.put("window_end", toEpochMillis(element.windowEnd));
        payload.put("total_amount", Base64.getEncoder().encodeToString(Decimal.fromLogical(DECIMAL_SCHEMA, element.totalAmount)));
        payload.put("total_units", element.totalUnits);
        payload.put("total_orders", element.totalOrders);
        payload.put("event_count", element.eventCount);
        payload.put("processed_at", toEpochMillis(element.processedAt));

        root.set("payload", payload);
        return root.toString();
    }

    private static ObjectNode buildSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "struct");
        schema.put("optional", false);
        schema.put("name", "com.greenteam.top_salesman.Value");

        ArrayNode fields = objectMapper.createArrayNode();
        fields.add(requiredField("schema_version", "string"));
        fields.add(requiredField("aggregation_type", "string"));
        fields.add(requiredField("salesman_id", "string"));
        fields.add(optionalField("country_id", "string"));
        fields.add(timestampField("window_start"));
        fields.add(timestampField("window_end"));
        fields.add(decimalField("total_amount", 2));
        fields.add(requiredField("total_units", "int64"));
        fields.add(requiredField("total_orders", "int64"));
        fields.add(requiredField("event_count", "int64"));
        fields.add(timestampField("processed_at"));
        schema.set("fields", fields);
        return schema;
    }

    private static ObjectNode requiredField(String fieldName, String type) {
        ObjectNode field = objectMapper.createObjectNode();
        field.put("field", fieldName);
        field.put("type", type);
        field.put("optional", false);
        return field;
    }

    private static ObjectNode optionalField(String fieldName, String type) {
        ObjectNode field = requiredField(fieldName, type);
        field.put("optional", true);
        return field;
    }

    private static ObjectNode decimalField(String fieldName, int scale) {
        ObjectNode field = requiredField(fieldName, "bytes");
        field.put("name", Decimal.LOGICAL_NAME);
        field.put("version", 1);

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put(Decimal.SCALE_FIELD, Integer.toString(scale));
        field.set("parameters", parameters);
        return field;
    }

    private static ObjectNode timestampField(String fieldName) {
        ObjectNode field = requiredField(fieldName, "int64");
        field.put("name", Timestamp.LOGICAL_NAME);
        field.put("version", 1);
        return field;
    }

    private static long toEpochMillis(String isoInstant) {
        return Instant.parse(isoInstant).toEpochMilli();
    }
}