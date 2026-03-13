package com.greenteam.operator;

import com.greenteam.model.CitySalesAccumulator;
import com.greenteam.model.CitySalesResult;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.math.RoundingMode;
import java.time.Instant;

public class CitySalesWindowFormatter
    extends ProcessWindowFunction<CitySalesAccumulator, CitySalesResult, String, TimeWindow> {

    @Override
    public void process(
        String compositeKey,
        Context context,
        Iterable<CitySalesAccumulator> elements,
        Collector<CitySalesResult> out
    ) {
        CitySalesAccumulator acc = elements.iterator().next();

        // compositeKey format: "storeId|saleDate"
        String[] parts = compositeKey.split("\\|", 2);
        int storeId = parts.length > 0 ? Integer.parseInt(parts[0]) : acc.storeId;
        String saleDate = parts.length > 1 ? parts[1] : acc.saleDate;
        String cityName = acc.cityName;
        String storeName = acc.storeName;

        String windowStart = Instant.ofEpochMilli(context.window().getStart()).toString();
        String windowEnd   = Instant.ofEpochMilli(context.window().getEnd()).toString();

        out.collect(new CitySalesResult(
            cityName,
            storeId,
            storeName,
            saleDate,
            windowStart,
            windowEnd,
            acc.totalAmount.setScale(2, RoundingMode.HALF_UP),
            acc.totalUnits,
            acc.saleIds.size(),
            acc.eventCount,
            Instant.now().toString()
        ));
    }
}

