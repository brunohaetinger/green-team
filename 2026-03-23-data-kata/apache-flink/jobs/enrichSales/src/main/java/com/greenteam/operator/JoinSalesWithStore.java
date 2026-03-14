package com.greenteam.operator;

import com.greenteam.model.PendingSalesByStore;
import com.greenteam.model.SaleWithStoreEvent;
import com.greenteam.model.SalesEvent;
import com.greenteam.model.StoreEvent;
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

public class JoinSalesWithStore extends KeyedCoProcessFunction<Integer, SalesEvent, StoreEvent, SaleWithStoreEvent> {

    private final long ttlMs;

    private transient ValueState<StoreEvent> storeState;
    private transient MapState<Integer, PendingSalesByStore> pendingState;

    private transient Counter pendingCounter;
    private transient Counter lateJoinCounter;
    private transient Counter ttlExpiredCounter;
    private transient AtomicLong pendingGaugeValue;

    public JoinSalesWithStore(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    @Override
    public void open(OpenContext openContext) {
        storeState = getRuntimeContext().getState(new ValueStateDescriptor<>("store-state", StoreEvent.class));
        pendingState = getRuntimeContext().getMapState(
            new MapStateDescriptor<>("pending-sales-by-store", Integer.class, PendingSalesByStore.class)
        );

        pendingCounter = getRuntimeContext().getMetricGroup().counter("pending_sales_missing_store_total");
        lateJoinCounter = getRuntimeContext().getMetricGroup().counter("late_store_join_total");
        ttlExpiredCounter = getRuntimeContext().getMetricGroup().counter("pending_sales_store_ttl_expired_total");

        pendingGaugeValue = new AtomicLong(0L);
        getRuntimeContext().getMetricGroup().gauge("pending_sales_missing_store_current", pendingGaugeValue::get);
    }

    @Override
    public void processElement1(SalesEvent sale, Context ctx, Collector<SaleWithStoreEvent> out) throws Exception {
        StoreEvent store = storeState.value();
        if (store != null) {
            out.collect(merge(sale, store));
            return;
        }

        long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
        pendingState.put(sale.saleId, new PendingSalesByStore(sale, expiresAt));
        ctx.timerService().registerProcessingTimeTimer(expiresAt);
        pendingCounter.inc();
        pendingGaugeValue.incrementAndGet();
    }

    @Override
    public void processElement2(StoreEvent store, Context ctx, Collector<SaleWithStoreEvent> out) throws Exception {
        storeState.update(store);

        Iterator<Map.Entry<Integer, PendingSalesByStore>> iterator = pendingState.entries().iterator();
        long now = ctx.timerService().currentProcessingTime();

        while (iterator.hasNext()) {
            Map.Entry<Integer, PendingSalesByStore> entry = iterator.next();
            PendingSalesByStore pending = entry.getValue();

            if (pending.expiresAt <= now) {
                iterator.remove();
                pendingGaugeValue.decrementAndGet();
                ttlExpiredCounter.inc();
                continue;
            }

            out.collect(merge(pending.sale, store));
            iterator.remove();
            pendingGaugeValue.decrementAndGet();
            lateJoinCounter.inc();
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<SaleWithStoreEvent> out) throws Exception {
        Iterator<Map.Entry<Integer, PendingSalesByStore>> iterator = pendingState.entries().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, PendingSalesByStore> entry = iterator.next();
            PendingSalesByStore pending = entry.getValue();
            if (pending.expiresAt <= timestamp) {
                iterator.remove();
                pendingGaugeValue.decrementAndGet();
                ttlExpiredCounter.inc();
            }
        }
    }

    private SaleWithStoreEvent merge(SalesEvent sale, StoreEvent store) {
        return new SaleWithStoreEvent(
            sale.salesmanId,
            sale.saleId,
            sale.quantity,
            sale.productId,
            sale.storeId,
            store.city,
            store.name,
            sale.saleDate,
            store.country,
            sale.amount
        );
    }
}