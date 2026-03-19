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
import java.util.UUID;

/*
 * This operator is responsible for parsing the raw JSON messages that are consumed from the sales topic in Kafka and converting them into SalesEvent objects that can be processed by the enrichment operators.
 * It uses the Jackson library to parse the JSON messages and extract the relevant fields to create the SalesEvent objects.
 * If a JSON message is malformed or does not contain the required fields, it increments a counter for malformed records and discards the message from the stream.
 */
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
            String eventId = JsonUtils.optionalText(node, "event_id");
            String traceId = JsonUtils.optionalText(node, "trace_id");

            if (saleId <= 0 || salesmanId <= 0 || storeId <= 0 || productId <= 0 || quantity <= 0) {
                malformedCounter.inc();
                return;
            }

            if (eventId == null) {
                eventId = UUID.randomUUID().toString();
            }
            if (traceId == null) {
                traceId = eventId;
            }

            out.collect(new SalesEvent(
                    eventId,
                    traceId,
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