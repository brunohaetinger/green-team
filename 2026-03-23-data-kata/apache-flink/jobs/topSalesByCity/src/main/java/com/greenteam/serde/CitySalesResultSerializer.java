package com.greenteam.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greenteam.config.JobConfig;
import com.greenteam.model.CitySalesResult;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.connect.data.Timestamp;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Base64;

public class CitySalesResultSerializer implements KafkaRecordSerializationSchema<CitySalesResult> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Schema DECIMAL_SCHEMA = Decimal.schema(2);

    // The serialize method converts a CitySalesResult object into a Kafka ProducerRecord with a specific key and value format.
    @Override
    public ProducerRecord<byte[], byte[]> serialize(
        CitySalesResult element,
        KafkaSinkContext context,
        Long timestamp
    ) {
        // Composite key format: "cityName|saleDate|windowEnd" - this allows us to easily identify the city and date for each record in Kafka.
        // we are using windowEnd in the key to ensure that records from different windows for the same city/date combination are treated as separate entries in Kafka.
        byte[] key = (element.cityName() + "|" + element.saleDate() + "|" + element.windowEnd()).getBytes(StandardCharsets.UTF_8);
        byte[] value = buildValue(element).getBytes(StandardCharsets.UTF_8);
        return new ProducerRecord<>(JobConfig.OUTPUT_TOPIC, key, value);
    }

    // This method builds the JSON value for the Kafka record, including the schema and payload.
    private static String buildValue(CitySalesResult element) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("schema", buildSchema());
    
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("city_name", element.cityName());
        payload.put("sale_date", element.saleDate().toEpochDay());
        payload.put("total_amount", Base64.getEncoder().encodeToString(Decimal.fromLogical(DECIMAL_SCHEMA, element.totalAmount())));
        payload.put("total_units", element.totalUnits());
        payload.put("processed_at", element.processedAt().toEpochMilli());
        payload.put("window_start", element.windowStart());
        payload.put("window_end", element.windowEnd());

        root.set("payload", payload);
        return root.toString();
    }

    // The buildSchema method constructs the JSON schema that describes the structure of the data being sent to Kafka. This includes field names, types, and any logical type information for complex types like decimals and dates.
    private static ObjectNode buildSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "struct");
        schema.put("optional", false);
        schema.put("name", "com.greenteam.total_sales.Value");

        ArrayNode fields = objectMapper.createArrayNode();
        fields.add(requiredField("city_name", "string"));
        fields.add(dateField("sale_date"));
        fields.add(decimalField("total_amount", 2));
        fields.add(requiredField("total_units", "int64"));
        fields.add(timestampField("processed_at"));
        fields.add(timestampField("window_start"));
        fields.add(timestampField("window_end"));
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

    private static ObjectNode decimalField(String fieldName, int scale) {
        ObjectNode field = requiredField(fieldName, "bytes");
        field.put("name", Decimal.LOGICAL_NAME);
        field.put("version", 1);

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put(Decimal.SCALE_FIELD, Integer.toString(scale));
        field.set("parameters", parameters);
        return field;
    }

    private static ObjectNode dateField(String fieldName) {
        ObjectNode field = requiredField(fieldName, "int32");
        field.put("name", Date.LOGICAL_NAME);
        field.put("version", 1);
        return field;
    }

    private static ObjectNode timestampField(String fieldName) {
        ObjectNode field = requiredField(fieldName, "int32");
        field.put("name", Timestamp.LOGICAL_NAME);
        field.put("version", 1);
        return field;
    }
}

