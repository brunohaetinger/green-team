package com.greenteam.operator;

import com.greenteam.model.SaleEvent;
import com.greenteam.model.TopSalesmanAccumulator;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.math.BigDecimal;

public class TopSalesmanAggregate implements AggregateFunction<SaleEvent, TopSalesmanAccumulator, TopSalesmanAccumulator> {
    @Override
    public TopSalesmanAccumulator createAccumulator() {
        return new TopSalesmanAccumulator();
    }

    @Override
    public TopSalesmanAccumulator add(SaleEvent event, TopSalesmanAccumulator acc) {
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
        acc.sourceEventCount += 1;
        return acc;
    }

    @Override
    public TopSalesmanAccumulator getResult(TopSalesmanAccumulator acc) {
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
        left.sourceEventCount += right.sourceEventCount;
        return left;
    }
}
