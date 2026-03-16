
package com.greenteam.operator;

import com.greenteam.model.TopSalesmanAccumulator;
import com.greenteam.model.TopSalesmanResult;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.math.RoundingMode;

public class TopSalesmanWindowFormatter
    extends ProcessWindowFunction<TopSalesmanAccumulator, TopSalesmanResult, String, TimeWindow> {

    @Override
    public void process(
        String key,
        Context context,
        Iterable<TopSalesmanAccumulator> elements,
        Collector<TopSalesmanResult> out
    ) {
        TopSalesmanAccumulator acc = elements.iterator().next();
        if (acc.totalUnits > 0) {
            out.collect(new TopSalesmanResult(
                acc.salesmanId,
                acc.salesmanName,
                acc.saleDate,
                acc.totalAmount.setScale(2, RoundingMode.HALF_UP),
                acc.totalUnits
            ));
        }
    }
}