package com.greenteam.operator;

import com.greenteam.model.CitySalesAccumulator;
import com.greenteam.model.SaleEvent;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.math.BigDecimal;

public class CitySalesAggregate
    implements AggregateFunction<SaleEvent, CitySalesAccumulator, CitySalesAccumulator> {

    @Override
    public CitySalesAccumulator createAccumulator() {
        return new CitySalesAccumulator();
    }

    @Override
    public CitySalesAccumulator add(SaleEvent event, CitySalesAccumulator acc) {
        acc.totalAmount = acc.totalAmount.add(event.amount.multiply(BigDecimal.valueOf(event.quantity)));
        acc.totalUnits += event.quantity;
        acc.eventCount += 1;
        acc.saleIds.add(event.saleId);

        if (acc.cityName == null && event.cityName != null) {
            acc.cityName = event.cityName;
        }
        if (acc.storeName == null && event.storeName != null) {
            acc.storeName = event.storeName;
        }
        if (acc.saleDate == null && event.saleDate != null) {
            acc.saleDate = event.saleDate;
        }

        return acc;
    }

    @Override
    public CitySalesAccumulator getResult(CitySalesAccumulator acc) {
        return acc;
    }

    @Override
    public CitySalesAccumulator merge(CitySalesAccumulator left, CitySalesAccumulator right) {
        left.totalAmount = left.totalAmount.add(right.totalAmount);
        left.totalUnits  += right.totalUnits;
        left.eventCount  += right.eventCount;
        left.saleIds.addAll(right.saleIds);

        return left;
    }
}

