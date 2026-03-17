package com.greenteam.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.ExpiredPendingSaleEvent;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class ExpiredPendingSaleEventSerializationSchema implements SerializationSchema<ExpiredPendingSaleEvent> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(ExpiredPendingSaleEvent event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ExpiredPendingSaleEvent", e);
        }
    }
}

