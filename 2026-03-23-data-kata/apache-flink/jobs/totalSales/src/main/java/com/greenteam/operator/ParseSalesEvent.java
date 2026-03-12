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

            String cityId    = JsonUtils.requiredText(node, "city_id");
            String saleId    = JsonUtils.requiredText(node, "sale_id");
            String countryId = JsonUtils.optionalText(node, "country_id");
            int    quantity  = node.path("quantity").asInt(-1);
            String amountRaw = node.path("amount").asText();

            if (quantity <= 0 || amountRaw.isBlank()) {
                return;
            }

            out.collect(new SaleEvent(cityId, countryId, saleId, quantity, new BigDecimal(amountRaw)));
        } catch (Exception ignored) {
            // Skip malformed records so the stream keeps running.
        }
    }
}

