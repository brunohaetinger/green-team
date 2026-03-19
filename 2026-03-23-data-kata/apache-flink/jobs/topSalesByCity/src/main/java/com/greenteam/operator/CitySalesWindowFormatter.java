package com.greenteam.operator;

import com.greenteam.model.CitySalesAccumulator;
import com.greenteam.model.CitySalesResult;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * ProcessWindowFunction to format the aggregated city sales data into a CitySalesResult.
 * It extracts city/store information and formats the window time range.
 * This executes after the aggregation step and prepares the final output for each window.
 */
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

        // compositeKey format: "cityName|saleDate"
        String[] parts = compositeKey.split("\\|", 2);
        String cityName = parts[0];
        LocalDate saleDate = LocalDate.parse(parts[1]);
        String windowId = compositeKey + "|" + context.window().getEnd();

        out.collect(new CitySalesResult(
                cityName,
                saleDate,
                acc.totalAmount.setScale(2, RoundingMode.HALF_UP),
                acc.totalUnits,
                acc.sourceEventCount,
                windowId,
                acc.processedAt,
                context.window().getStart(),
                context.window().getEnd()
        ));
    }
}

