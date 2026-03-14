package com.greenteam.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenteam.model.SalesEvent;
import com.greenteam.util.JsonUtils;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;

public class ParseSalesEvent extends RichFlatMapFunction<String, SalesEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private transient Counter malformedCounter;

    @Override
    public void open(OpenContext openContext) {
        malformedCounter = getRuntimeContext().getMetricGroup().counter("malformed_sales_records");
    }

    @Override
    public void flatMap(String message, Collector<SalesEvent> out) {
        try {
            JsonNode node = JsonUtils.resolvePayload(OBJECT_MAPPER.readTree(message));

            int saleId = JsonUtils.requiredInt(node, "id");
            int salesmanId = JsonUtils.requiredInt(node, "salesman_id");
            int storeId = JsonUtils.requiredInt(node, "store_id");
            int productId = JsonUtils.requiredInt(node, "product_id");
            int quantity = JsonUtils.requiredInt(node, "quantity");
            String saleDate = JsonUtils.requiredText(node, "sale_date");
            String amountRaw = JsonUtils.requiredText(node, "amount");

            if (saleId <= 0 || salesmanId <= 0 || storeId <= 0 || productId <= 0 || quantity <= 0) {
                malformedCounter.inc();
                return;
            }

            out.collect(new SalesEvent(
                saleId,
                salesmanId,
                storeId,
                new BigDecimal(amountRaw),
                saleDate,
                productId,
                quantity
            ));
        } catch (Exception ignored) {
            malformedCounter.inc();
        }
    }
}