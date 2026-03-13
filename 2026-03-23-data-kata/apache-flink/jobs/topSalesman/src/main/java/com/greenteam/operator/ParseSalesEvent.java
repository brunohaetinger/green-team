package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.SaleEvent;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;

public class ParseSalesEvent implements FlatMapFunction<String, SaleEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void flatMap(String message, Collector<SaleEvent> out) {
        try {
            JsonNode node = objectMapper.readTree(message);

            int salesmanId = node.path("salesman_id").asInt(-1);
            String salesmanName = JsonUtils.requiredText(node, "salesman_name");
            String saleDateRaw = JsonUtils.requiredText(node, "sale_date");
            int saleId = node.path("sale_id").asInt(-1);
            int quantity = node.path("quantity").asInt(-1);
            String amountRaw = node.path("amount").asText();

            if (salesmanId < 0 || saleId < 0 || quantity <= 0 || amountRaw.isBlank()) {
                return;
            }

            String saleDate = saleDateRaw.length() >= 10 ? saleDateRaw.substring(0, 10) : saleDateRaw;

            out.collect(new SaleEvent(salesmanId, salesmanName, saleDate, saleId, quantity, new BigDecimal(amountRaw)));
        } catch (Exception ignored) {
            // Skip malformed records so the stream keeps running.
        }
    }
}