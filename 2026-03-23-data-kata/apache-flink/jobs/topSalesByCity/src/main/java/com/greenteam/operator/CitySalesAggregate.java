package com.greenteam.operator;

import com.greenteam.model.CitySalesAccumulator;
import com.greenteam.model.SaleEvent;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.math.BigDecimal;

/**
 * AggregateFunction to compute total sales amount and total units sold for each city.
 * It also collects unique sale IDs and retains city/store information.
 * This is used in the aggregation step of the Flink job to combine SaleEvent records into a CitySalesAccumulator for each city/store/date key.
 * The resulting CitySalesAccumulator is then processed by CitySalesWindowFormatter to produce the final CitySalesResult for each window.
 */
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
        return left;
    }
}

