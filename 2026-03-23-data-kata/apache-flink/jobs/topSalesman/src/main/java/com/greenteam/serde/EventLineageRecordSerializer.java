package com.greenteam.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greenteam.config.JobConfig;
import com.greenteam.model.EventLineageRecord;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

public class EventLineageRecordSerializer implements KafkaRecordSerializationSchema<EventLineageRecord> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ProducerRecord<byte[], byte[]> serialize(EventLineageRecord element, KafkaSinkContext context, Long timestamp) {
        byte[] key = element.eventId().getBytes(StandardCharsets.UTF_8);
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("event_id", element.eventId());
        root.put("trace_id", element.traceId());
        root.put("sale_id", element.saleId());
        root.put("job_name", element.jobName());
        root.put("stage", element.stage());
        root.put("input_topic", element.inputTopic());
        root.put("output_topic", element.outputTopic());
        root.put("aggregation_key", element.aggregationKey());
        root.put("processed_at", element.processedAt());
        return new ProducerRecord<>(JobConfig.EVENT_LINEAGE_TOPIC, key, root.toString().getBytes(StandardCharsets.UTF_8));
    }
}
