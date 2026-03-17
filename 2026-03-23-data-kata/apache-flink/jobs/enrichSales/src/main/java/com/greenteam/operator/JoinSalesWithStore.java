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

/*
    * This operator is responsible for joining the sales events with the store information. 
    * It uses a keyed state to store the store information and a MapState to store the pending sales that are waiting for their corresponding store information. 
    * When a new sale event is received, it checks if the corresponding store information is already stored in the state. If it is, it enriches the sale event with the store information and emits it to the output topic. 
    * If the store information is not stored in the state, it adds the sale event to the pending state and waits for the corresponding store information to arrive. 
    * When a new store event is received, it updates the store information in the state and checks if there are any pending sales that can be enriched with the new store information. If there are, it enriches them and emits them to the output topic.
*/
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

    // The open method is called when the operator is initialized. It is used to set up the state and metrics for the operator.
    // We create a ValueState to store the store information, and a MapState to store the pending sales that are waiting for their corresponding store information.
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

    // The processElement1 method is called whenever a new sale event is received.
    // It checks if the corresponding store information is already stored in the state.
    // If it is, it enriches the sale event with the store information and emits it to the output topic.
    // If the store information is not stored in the state, it adds the sale event to the pending state and waits for the corresponding store information to arrive.
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

    // The processElement2 method is called whenever a new store event is received. 
    // It updates the store information in the state and checks if there are any pending sales that can be enriched with the new store information.
    // We iterate over the pending sales in the MapState and check if any of them can be enriched with the new store information.
    // If a pending sale can be enriched, we emit the enriched sale event to the output topic and remove the pending sale from the state. 
    // If a pending sale has expired, we remove it from the state and increment the TTL expired counter. 
    // If a pending sale is enriched with the store information, we increment the late join counter, 
    // which indicates that the store information arrived after the sale event, but we were still able to successfully enrich the sale event with the store
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

    // The onTimer method is called whenever a timer that we registered for a pending sale expires.
    // We iterate over the pending sales in the MapState and check if any of them have expired. 
    // If a pending sale has expired, we remove it from the state and increment the TTL expired counter,
    // which indicates that the store information did not arrive within the TTL defined in the JobConfig and the pending sale was discarded from the state.
    // This method is important for cleaning up expired pending sales from the state and preventing the state from growing indefinitely with pending sales that will never be enriched with the store information
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

    // The merge method is a helper method that takes a sale event and a store event, 
    // and merges them into a single SaleWithStoreEvent that contains all the information from both events.
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