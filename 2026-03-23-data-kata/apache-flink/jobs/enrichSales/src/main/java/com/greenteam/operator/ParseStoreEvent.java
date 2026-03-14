package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.StoreEvent;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;

public class ParseStoreEvent extends RichFlatMapFunction<String, StoreEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private transient Counter malformedCounter;

    @Override
    public void open(OpenContext openContext) {
        malformedCounter = getRuntimeContext().getMetricGroup().counter("malformed_stores_records");
    }

    @Override
    public void flatMap(String message, Collector<StoreEvent> out) {
        try {
            JsonNode node = JsonUtils.resolvePayload(OBJECT_MAPPER.readTree(message));

            int id = JsonUtils.requiredInt(node, "id");
            String name = JsonUtils.requiredText(node, "name");
            String city = JsonUtils.requiredText(node, "city");
            String state = JsonUtils.requiredText(node, "state");
            String country = JsonUtils.requiredText(node, "country");

            if (id <= 0) {
                malformedCounter.inc();
                return;
            }

            out.collect(new StoreEvent(id, name, city, state, JsonUtils.normalizeCountry(country)));
        } catch (Exception ignored) {
            malformedCounter.inc();
        }
    }
}