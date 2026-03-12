package com.greenteam.serde;

import com.greenteam.config.JobConfig;
import com.greenteam.model.CitySalesResult;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

public class CitySalesResultSerializer implements KafkaRecordSerializationSchema<CitySalesResult> {

    @Override
    public ProducerRecord<byte[], byte[]> serialize(
        CitySalesResult element,
        KafkaSinkContext context,
        Long timestamp
    ) {
        byte[] key   = (element.cityId + "|" + element.windowEnd).getBytes(StandardCharsets.UTF_8);
        byte[] value = element.payload.getBytes(StandardCharsets.UTF_8);
        return new ProducerRecord<>(JobConfig.OUTPUT_TOPIC, key, value);
    }
}

