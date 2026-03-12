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
        String cityId,
        Context context,
        Iterable<CitySalesAccumulator> elements,
        Collector<CitySalesResult> out
    ) {
        CitySalesAccumulator acc = elements.iterator().next();

        String windowStart = Instant.ofEpochMilli(context.window().getStart()).toString();
        String windowEnd   = Instant.ofEpochMilli(context.window().getEnd()).toString();

        out.collect(new CitySalesResult(
            cityId,
            acc.countryId,
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

