package com.greenteam.operator;

import com.greenteam.model.ExpiredPendingSaleEvent;
import com.greenteam.model.ExpiredReason;
import com.greenteam.model.SaleWithStoreEvent;
import com.greenteam.model.SalesEvent;
import com.greenteam.model.StoreEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * Operator that enriches SalesEvent with StoreEvent in parallel.
 * Emits SaleWithStoreEvent when enrichment is possible.
 * Emits ExpiredPendingSaleEvent if enrichment expires.
 */
public class EnrichSalesWithStore extends KeyedCoProcessFunction<Integer, SalesEvent, StoreEvent, SaleWithStoreEvent> {
    public static final OutputTag<ExpiredPendingSaleEvent> EXPIRED_SALES_TAG = new OutputTag<ExpiredPendingSaleEvent>("expired-sales-store") {
    };

    private final long ttlMs;
    private transient ValueState<SalesEvent> pendingSaleState;
    private transient ValueState<StoreEvent> storeState;
    private transient ValueState<Long> expirationState;

    public EnrichSalesWithStore(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    @Override
    public void open(org.apache.flink.api.common.functions.OpenContext openContext) {
        pendingSaleState = getRuntimeContext().getState(new ValueStateDescriptor<>("pending-sale", SalesEvent.class));
        storeState = getRuntimeContext().getState(new ValueStateDescriptor<>("store", StoreEvent.class));
        expirationState = getRuntimeContext().getState(new ValueStateDescriptor<>("store-expiration", Long.class));
    }

    @Override
    public void processElement1(SalesEvent sale, Context ctx, Collector<SaleWithStoreEvent> out) throws Exception {
        StoreEvent store = storeState.value();
        if (store != null) {
            out.collect(merge(sale, store));
            pendingSaleState.clear();
            storeState.clear();
            expirationState.clear();
        } else {
            pendingSaleState.update(sale);
            long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
            expirationState.update(expiresAt);
            ctx.timerService().registerProcessingTimeTimer(expiresAt);
        }
    }

    @Override
    public void processElement2(StoreEvent store, Context ctx, Collector<SaleWithStoreEvent> out) throws Exception {
        SalesEvent sale = pendingSaleState.value();
        if (sale != null) {
            out.collect(merge(sale, store));
            pendingSaleState.clear();
            storeState.clear();
            expirationState.clear();
        } else {
            storeState.update(store);
            long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
            expirationState.update(expiresAt);
            ctx.timerService().registerProcessingTimeTimer(expiresAt);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<SaleWithStoreEvent> out) throws Exception {
        SalesEvent sale = pendingSaleState.value();
        StoreEvent store = storeState.value();
        if (sale == null || store == null) {
            ExpiredPendingSaleEvent expired = new ExpiredPendingSaleEvent();
            expired.saleId = ctx.getCurrentKey();
            expired.eventId = sale != null ? sale.eventId : Integer.toString(ctx.getCurrentKey());
            expired.traceId = sale != null ? sale.traceId : expired.eventId;
            expired.expiresAt = timestamp;
            expired.reason = ExpiredReason.STORE_TTL_EXPIRED;
            ctx.output(EXPIRED_SALES_TAG, expired);
            pendingSaleState.clear();
            storeState.clear();
            expirationState.clear();
        }
    }

    private SaleWithStoreEvent merge(SalesEvent sale, StoreEvent store) {
        return new SaleWithStoreEvent(
                sale.eventId,
                sale.traceId,
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

