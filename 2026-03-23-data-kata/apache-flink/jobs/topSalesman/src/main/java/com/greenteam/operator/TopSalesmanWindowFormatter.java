package com.greenteam.operator;

import com.greenteam.model.SaleEvent;
import com.greenteam.model.TopSalesmanResult;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class TopSalesmanWindowFormatter
    extends ProcessWindowFunction<SaleEvent, TopSalesmanResult, String, TimeWindow> {

    @Override
    public void process(
        String saleDate,
        Context context,
        Iterable<SaleEvent> events,
        Collector<TopSalesmanResult> out
    ) {
        Map<Integer, RunningTotals> totalsBySalesman = new HashMap<>();

        for (SaleEvent event : events) {
            RunningTotals totals = totalsBySalesman.computeIfAbsent(event.salesmanId, key -> new RunningTotals());

            totals.totalAmount = totals.totalAmount.add(event.amount.multiply(BigDecimal.valueOf(event.quantity)));
            totals.totalUnits += event.quantity;
            if (totals.salesmanName == null && event.salesmanName != null) {
                totals.salesmanName = event.salesmanName;
            }
        }

        if (totalsBySalesman.isEmpty()) {
            return;
        }

        Integer topSalesmanId = null;
        String topSalesmanName = null;
        RunningTotals topTotals = null;

        for (Map.Entry<Integer, RunningTotals> entry : totalsBySalesman.entrySet()) {
            if (isBetterCandidate(entry.getKey(), entry.getValue(), topSalesmanId, topTotals)) {
                topSalesmanId = entry.getKey();
                topSalesmanName = entry.getValue().salesmanName;
                topTotals = entry.getValue();
            }
        }

        out.collect(new TopSalesmanResult(
            topSalesmanId,
            topSalesmanName,
            saleDate,
            topTotals.totalAmount.setScale(2, RoundingMode.HALF_UP),
            topTotals.totalUnits
        ));
    }

    private static boolean isBetterCandidate(
        Integer candidateId,
        RunningTotals candidate,
        Integer currentId,
        RunningTotals current
    ) {
        if (current == null) {
            return true;
        }

        int amountCompare = candidate.totalAmount.compareTo(current.totalAmount);
        if (amountCompare != 0) {
            return amountCompare > 0;
        }

        if (candidate.totalUnits != current.totalUnits) {
            return candidate.totalUnits > current.totalUnits;
        }

        return Integer.compare(candidateId, currentId) < 0;
    }

    private static final class RunningTotals {
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private long totalUnits = 0;
        private String salesmanName;
    }
}