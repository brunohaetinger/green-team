package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.StoreEvent;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;

/*
    * This operator is responsible for parsing the raw JSON messages that are consumed from the stores topic in Kafka and converting them into StoreEvent objects that can be processed by the enrichment operators. 
    * It uses the Jackson library to parse the JSON messages and extract the relevant fields to create the StoreEvent objects. 
    * If a JSON message is malformed or does not contain the required fields, it increments a counter for malformed records and discards the message from the stream.
*/
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