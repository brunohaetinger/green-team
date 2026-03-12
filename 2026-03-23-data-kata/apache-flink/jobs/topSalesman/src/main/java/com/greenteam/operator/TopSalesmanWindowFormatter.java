package com.greenteam.operator;

import com.greenteam.model.SaleEvent;
import com.greenteam.model.TopSalesmanResult;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TopSalesmanWindowFormatter
    extends ProcessAllWindowFunction<SaleEvent, TopSalesmanResult, TimeWindow> {

    @Override
    public void process(
        Context context,
        Iterable<SaleEvent> events,
        Collector<TopSalesmanResult> out
    ) {
        Map<String, RunningTotals> totalsBySalesman = new HashMap<>();

        for (SaleEvent event : events) {
            RunningTotals totals = totalsBySalesman.computeIfAbsent(event.salesmanId, key -> new RunningTotals());

            totals.totalAmount = totals.totalAmount.add(event.amount.multiply(BigDecimal.valueOf(event.quantity)));
            totals.totalUnits += event.quantity;
            totals.eventCount += 1;
            totals.saleIds.add(event.saleId);

            if (totals.countryId == null && event.countryId != null) {
                totals.countryId = event.countryId;
            }
        }

        if (totalsBySalesman.isEmpty()) {
            return;
        }

        String topSalesmanId = null;
        RunningTotals topTotals = null;

        for (Map.Entry<String, RunningTotals> entry : totalsBySalesman.entrySet()) {
            if (isBetterCandidate(entry.getKey(), entry.getValue(), topSalesmanId, topTotals)) {
                topSalesmanId = entry.getKey();
                topTotals = entry.getValue();
            }
        }

        String windowStart = Instant.ofEpochMilli(context.window().getStart()).toString();
        String windowEnd = Instant.ofEpochMilli(context.window().getEnd()).toString();

        out.collect(new TopSalesmanResult(
            topSalesmanId,
            topTotals.countryId,
            windowStart,
            windowEnd,
            topTotals.totalAmount.setScale(2, RoundingMode.HALF_UP),
            topTotals.totalUnits,
            topTotals.saleIds.size(),
            topTotals.eventCount,
            Instant.now().toString()
        ));
    }

    private static boolean isBetterCandidate(
        String candidateId,
        RunningTotals candidate,
        String currentId,
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

        if (candidate.eventCount != current.eventCount) {
            return candidate.eventCount > current.eventCount;
        }

        return candidateId.compareTo(currentId) < 0;
    }

    private static final class RunningTotals {
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private long totalUnits = 0;
        private long eventCount = 0;
        private String countryId;
        private final Set<String> saleIds = new HashSet<>();
    }
}