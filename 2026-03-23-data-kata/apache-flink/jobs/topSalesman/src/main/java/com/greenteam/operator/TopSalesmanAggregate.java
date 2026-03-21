package com.greenteam.operator;

import com.greenteam.model.SaleEnriched;
import com.greenteam.model.TopSalesmanAccumulator;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.math.BigDecimal;
import java.time.Instant;

public class TopSalesmanAggregate implements AggregateFunction<SaleEnriched, TopSalesmanAccumulator, TopSalesmanAccumulator> {
    @Override
    public TopSalesmanAccumulator createAccumulator() {
        return new TopSalesmanAccumulator();
    }

    @Override
    public TopSalesmanAccumulator add(SaleEnriched event, TopSalesmanAccumulator acc) {
        if (acc.salesmanId < 0) {
            acc.salesmanId = event.salesmanId;
        }
        if (acc.salesmanName == null && event.salesmanName != null) {
            acc.salesmanName = event.salesmanName;
        }
        if (acc.saleDate == null && event.saleDate != null) {
            acc.saleDate = event.saleDate;
        }
        acc.totalAmount = acc.totalAmount.add(event.amount.multiply(BigDecimal.valueOf(event.quantity)));
        acc.totalUnits += event.quantity;
        return acc;
    }

    @Override
    public TopSalesmanAccumulator getResult(TopSalesmanAccumulator acc) {
        acc.processedAt = Instant.now();
        return acc;
    }

    @Override
    public TopSalesmanAccumulator merge(TopSalesmanAccumulator left, TopSalesmanAccumulator right) {
        left.totalAmount = left.totalAmount.add(right.totalAmount);
        left.totalUnits += right.totalUnits;
        if (left.salesmanName == null) {
            left.salesmanName = right.salesmanName;
        }
        if (left.saleDate == null) {
            left.saleDate = right.saleDate;
        }
        if (left.salesmanId < 0) {
            left.salesmanId = right.salesmanId;
        }
        return left;
    }
}
