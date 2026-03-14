package com.greenteam.operator;

import com.greenteam.model.PendingSalesBySalesman;
import com.greenteam.model.SaleWithStoreEvent;
import com.greenteam.model.SalesEnrichedEvent;
import com.greenteam.model.SalesmanEvent;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class JoinSalesWithSalesman extends KeyedCoProcessFunction<Integer, SaleWithStoreEvent, SalesmanEvent, SalesEnrichedEvent> {

    private final long ttlMs;

    private transient ValueState<SalesmanEvent> salesmanState;
    private transient MapState<Integer, PendingSalesBySalesman> pendingState;

    private transient Counter pendingCounter;
    private transient Counter lateJoinCounter;
    private transient Counter ttlExpiredCounter;
    private transient AtomicLong pendingGaugeValue;

    public JoinSalesWithSalesman(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    @Override
    public void open(OpenContext openContext) {
        salesmanState = getRuntimeContext().getState(new ValueStateDescriptor<>("salesman-state", SalesmanEvent.class));
        pendingState = getRuntimeContext().getMapState(
            new MapStateDescriptor<>("pending-sales-by-salesman", Integer.class, PendingSalesBySalesman.class)
        );

        pendingCounter = getRuntimeContext().getMetricGroup().counter("pending_sales_missing_salesman_total");
        lateJoinCounter = getRuntimeContext().getMetricGroup().counter("late_salesman_join_total");
        ttlExpiredCounter = getRuntimeContext().getMetricGroup().counter("pending_sales_salesman_ttl_expired_total");

        pendingGaugeValue = new AtomicLong(0L);
        getRuntimeContext().getMetricGroup().gauge("pending_sales_missing_salesman_current", pendingGaugeValue::get);
    }

    @Override
    public void processElement1(SaleWithStoreEvent sale, Context ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        SalesmanEvent salesman = salesmanState.value();
        if (salesman != null) {
            out.collect(merge(sale, salesman));
            return;
        }

        long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
        pendingState.put(sale.saleId, new PendingSalesBySalesman(sale, expiresAt));
        ctx.timerService().registerProcessingTimeTimer(expiresAt);
        pendingCounter.inc();
        pendingGaugeValue.incrementAndGet();
    }

    @Override
    public void processElement2(SalesmanEvent salesman, Context ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        salesmanState.update(salesman);

        Iterator<Map.Entry<Integer, PendingSalesBySalesman>> iterator = pendingState.entries().iterator();
        long now = ctx.timerService().currentProcessingTime();

        while (iterator.hasNext()) {
            Map.Entry<Integer, PendingSalesBySalesman> entry = iterator.next();
            PendingSalesBySalesman pending = entry.getValue();

            if (pending.expiresAt <= now) {
                iterator.remove();
                pendingGaugeValue.decrementAndGet();
                ttlExpiredCounter.inc();
                continue;
            }

            out.collect(merge(pending.sale, salesman));
            iterator.remove();
            pendingGaugeValue.decrementAndGet();
            lateJoinCounter.inc();
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        Iterator<Map.Entry<Integer, PendingSalesBySalesman>> iterator = pendingState.entries().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, PendingSalesBySalesman> entry = iterator.next();
            PendingSalesBySalesman pending = entry.getValue();
            if (pending.expiresAt <= timestamp) {
                iterator.remove();
                pendingGaugeValue.decrementAndGet();
                ttlExpiredCounter.inc();
            }
        }
    }

    private SalesEnrichedEvent merge(SaleWithStoreEvent sale, SalesmanEvent salesman) {
        return new SalesEnrichedEvent(
            sale.salesmanId,
            salesman.name,
            sale.saleId,
            sale.quantity,
            sale.productId,
            sale.storeId,
            sale.cityName,
            sale.storeName,
            sale.saleDate,
            sale.countryName,
            sale.amount
        );
    }
}