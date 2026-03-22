package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.SaleEnriched;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * FlatMapFunction to parse raw JSON strings into SaleEvent objects.
 * It handles malformed records gracefully by skipping them.
 */
public class ParseSalesEvent implements FlatMapFunction<String, SaleEnriched> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void flatMap(String message, Collector<SaleEnriched> out) {
        try {
            JsonNode node = objectMapper.readTree(message);

            String cityName = JsonUtils.requiredText(node, "city_name");
            String saleDateRaw = JsonUtils.requiredText(node, "sale_date");
            int quantity = node.path("quantity").asInt(-1);
            String amountRaw = node.path("amount").asText();

            if (quantity <= 0 || amountRaw.isBlank()) {
                return;
            }

            // Truncate ISO timestamp to date ("2026-03-13T12:00:00Z" -> "2026-03-13")
            LocalDate saleDate = Instant.parse(saleDateRaw).atZone(ZoneId.of("UTC")).toLocalDate();
            out.collect(new SaleEnriched(
                    0,
                    null,
                    0,
                    quantity,
                    0,
                    null,
                    cityName,
                    null,
                    saleDate,
                    null,
                    new BigDecimal(amountRaw)
            ));
        } catch (Exception ignored) {
            // Skip malformed records so the stream keeps running.
        }
    }
}

