package com.greenteam.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greenteam.model.CitySalesAccumulator;
import com.greenteam.model.CitySalesResult;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.math.RoundingMode;
import java.time.Instant;

public class CitySalesWindowFormatter
    extends ProcessWindowFunction<CitySalesAccumulator, CitySalesResult, String, TimeWindow> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(
        String cityId,
        Context context,
        Iterable<CitySalesAccumulator> elements,
        Collector<CitySalesResult> out
    ) {
        CitySalesAccumulator acc = elements.iterator().next();

        String windowStart = Instant.ofEpochMilli(context.window().getStart()).toString();
        String windowEnd   = Instant.ofEpochMilli(context.window().getEnd()).toString();

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("schema_version",   "1.0");
        payload.put("aggregation_type", "city_sales");
        payload.put("city_id",          cityId);

        if (acc.countryId != null) {
            payload.put("country_id", acc.countryId);
        }

        payload.put("window_start",  windowStart);
        payload.put("window_end",    windowEnd);
        payload.put("total_amount",  acc.totalAmount.setScale(2, RoundingMode.HALF_UP));
        payload.put("total_units",   acc.totalUnits);
        payload.put("total_orders",  acc.saleIds.size());
        payload.put("event_count",   acc.eventCount);
        payload.put("processed_at",  Instant.now().toString());

        out.collect(new CitySalesResult(cityId, windowEnd, payload.toString()));
    }
}

