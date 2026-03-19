package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.SaleEvent;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.util.UUID;

public class ParseSalesEvent implements FlatMapFunction<String, SaleEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void flatMap(String message, Collector<SaleEvent> out) {
        try {
            JsonNode node = objectMapper.readTree(message);

            int saleId = node.path("sale_id").asInt(-1);
            int salesmanId = node.path("salesman_id").asInt(-1);
            String salesmanName = JsonUtils.requiredText(node, "salesman_name");
            String saleDateRaw = JsonUtils.requiredText(node, "sale_date");
            int quantity = node.path("quantity").asInt(-1);
            String amountRaw = node.path("amount").asText();
            String eventId = node.path("event_id").asText(null);
            String traceId = node.path("trace_id").asText(null);

            if (salesmanId < 0 || quantity <= 0 || amountRaw.isBlank() || saleId <= 0) {
                return;
            }

            if (eventId == null || eventId.isBlank()) {
                eventId = UUID.randomUUID().toString();
            }
            if (traceId == null || traceId.isBlank()) {
                traceId = eventId;
            }

            String saleDate = saleDateRaw.length() >= 10 ? saleDateRaw.substring(0, 10) : saleDateRaw;

            out.collect(new SaleEvent(eventId, traceId, saleId, salesmanId, salesmanName, saleDate, quantity, new BigDecimal(amountRaw)));
        } catch (Exception ignored) {
            // Skip malformed records so the stream keeps running.
        }
    }
}