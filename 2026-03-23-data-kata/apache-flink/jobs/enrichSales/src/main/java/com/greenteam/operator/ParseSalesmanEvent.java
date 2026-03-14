package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.SalesmanEvent;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;

public class ParseSalesmanEvent extends RichFlatMapFunction<String, SalesmanEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private transient Counter malformedCounter;

    @Override
    public void open(OpenContext openContext) {
        malformedCounter = getRuntimeContext().getMetricGroup().counter("malformed_salesmans_records");
    }

    @Override
    public void flatMap(String message, Collector<SalesmanEvent> out) {
        try {
            JsonNode node = JsonUtils.resolvePayload(OBJECT_MAPPER.readTree(message));

            int id = JsonUtils.requiredInt(node, "id");
            String name = JsonUtils.requiredText(node, "name");
            int storeId = JsonUtils.requiredInt(node, "store_id");

            if (id <= 0 || storeId <= 0) {
                malformedCounter.inc();
                return;
            }

            out.collect(new SalesmanEvent(id, name, storeId));
        } catch (Exception ignored) {
            malformedCounter.inc();
        }
    }
}