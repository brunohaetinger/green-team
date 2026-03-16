package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.SaleEvent;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;

/**
 * FlatMapFunction to parse raw JSON strings into SaleEvent objects.
 * It handles malformed records gracefully by skipping them.
 */
public class ParseSalesEvent implements FlatMapFunction<String, SaleEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void flatMap(String message, Collector<SaleEvent> out) {
        try {
            JsonNode node = objectMapper.readTree(message);

            String cityName    = JsonUtils.requiredText(node, "city_name");
            int    storeId     = node.path("store_id").asInt(-1);
            String storeName   = JsonUtils.requiredText(node, "store_name");
            String saleDateRaw = JsonUtils.requiredText(node, "sale_date");
            int    saleId      = node.path("sale_id").asInt(-1);
            int    quantity    = node.path("quantity").asInt(-1);
            String amountRaw   = node.path("amount").asText();

            if (storeId < 0 || saleId < 0 || quantity <= 0 || amountRaw.isBlank()) {
                return;
            }

            // Truncate ISO timestamp to date ("2026-03-13T12:00:00Z" -> "2026-03-13")
            String saleDate = saleDateRaw.length() >= 10 ? saleDateRaw.substring(0, 10) : saleDateRaw;

            out.collect(new SaleEvent(cityName, storeId, storeName, saleDate, saleId, quantity, new BigDecimal(amountRaw)));
        } catch (Exception ignored) {
            // Skip malformed records so the stream keeps running.
        }
    }
}

