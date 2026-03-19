package com.greenteam.operator;

import com.greenteam.model.ExpiredPendingSaleEvent;
import com.greenteam.model.ExpiredReason;
import com.greenteam.model.SaleWithSalesmanEvent;
import com.greenteam.model.SalesEvent;
import com.greenteam.model.SalesmanEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * Operator that enriches SalesEvent with SalesmanEvent in parallel.
 * Emits SaleWithSalesmanEvent when enrichment is possible.
 * Emits ExpiredPendingSaleEvent if enrichment expires.
 */
public class EnrichSalesWithSalesman extends KeyedCoProcessFunction<Integer, SalesEvent, SalesmanEvent, SaleWithSalesmanEvent> {
    public static final OutputTag<ExpiredPendingSaleEvent> EXPIRED_SALES_TAG = new OutputTag<ExpiredPendingSaleEvent>("expired-sales-salesman") {
    };

    private final long ttlMs;
    private transient ValueState<SalesEvent> pendingSaleState;
    private transient ValueState<SalesmanEvent> salesmanState;
    private transient ValueState<Long> expirationState;

    public EnrichSalesWithSalesman(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    @Override
    public void open(org.apache.flink.api.common.functions.OpenContext openContext) {
        pendingSaleState = getRuntimeContext().getState(new ValueStateDescriptor<>("pending-sale", SalesEvent.class));
        salesmanState = getRuntimeContext().getState(new ValueStateDescriptor<>("salesman", SalesmanEvent.class));
        expirationState = getRuntimeContext().getState(new ValueStateDescriptor<>("salesman-expiration", Long.class));
    }

    @Override
    public void processElement1(SalesEvent sale, Context ctx, Collector<SaleWithSalesmanEvent> out) throws Exception {
        SalesmanEvent salesman = salesmanState.value();
        if (salesman != null) {
            out.collect(merge(sale, salesman));
            pendingSaleState.clear();
            salesmanState.clear();
            expirationState.clear();
        } else {
            pendingSaleState.update(sale);
            long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
            expirationState.update(expiresAt);
            ctx.timerService().registerProcessingTimeTimer(expiresAt);
        }
    }

    @Override
    public void processElement2(SalesmanEvent salesman, Context ctx, Collector<SaleWithSalesmanEvent> out) throws Exception {
        SalesEvent sale = pendingSaleState.value();
        if (sale != null) {
            out.collect(merge(sale, salesman));
            pendingSaleState.clear();
            salesmanState.clear();
            expirationState.clear();
        } else {
            salesmanState.update(salesman);
            long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
            expirationState.update(expiresAt);
            ctx.timerService().registerProcessingTimeTimer(expiresAt);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<SaleWithSalesmanEvent> out) throws Exception {
        SalesEvent sale = pendingSaleState.value();
        SalesmanEvent salesman = salesmanState.value();
        if (sale == null || salesman == null) {
            ExpiredPendingSaleEvent expired = new ExpiredPendingSaleEvent();
            expired.saleId = ctx.getCurrentKey();
            expired.eventId = sale != null ? sale.eventId : Integer.toString(ctx.getCurrentKey());
            expired.traceId = sale != null ? sale.traceId : expired.eventId;
            expired.expiresAt = timestamp;
            expired.reason = ExpiredReason.SALESMAN_TTL_EXPIRED;
            ctx.output(EXPIRED_SALES_TAG, expired);
            pendingSaleState.clear();
            salesmanState.clear();
            expirationState.clear();
        }
    }

    private SaleWithSalesmanEvent merge(SalesEvent sale, SalesmanEvent salesman) {
        return new SaleWithSalesmanEvent(
                sale.eventId,
                sale.traceId,
                sale.saleId,
                salesman.id,
                salesman.name,
                sale.saleDate,
                sale.productId,
                sale.storeId,
                sale.quantity
        );
    }
}

