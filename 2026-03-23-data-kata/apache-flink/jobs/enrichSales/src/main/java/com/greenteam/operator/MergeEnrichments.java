package com.greenteam.operator;

import com.greenteam.model.SaleWithStoreEvent;
import com.greenteam.model.SaleWithSalesmanEvent;
import com.greenteam.model.SalesEnrichedEvent;
import com.greenteam.model.ExpiredPendingSaleEvent;
import com.greenteam.model.ExpiredReason;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * Operator that merges partial enrichments (store and salesman) for a sale.
 * Emits SalesEnrichedEvent when both enrichments are available.
 * Emits ExpiredPendingSaleEvent if any enrichment expires.
 */
public class MergeEnrichments extends KeyedCoProcessFunction<Integer, SaleWithStoreEvent, SaleWithSalesmanEvent, SalesEnrichedEvent> {
    public static final OutputTag<ExpiredPendingSaleEvent> EXPIRED_SALES_TAG = new OutputTag<>("expired-sales-merge");

    private final long ttlMs;
    private transient ValueState<SaleWithStoreEvent> storeState;
    private transient ValueState<SaleWithSalesmanEvent> salesmanState;
    private transient ValueState<Long> expirationState;

    public MergeEnrichments(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    @Override
    public void open(org.apache.flink.api.common.functions.OpenContext openContext) {
        storeState = getRuntimeContext().getState(new ValueStateDescriptor<>("store-enrichment", SaleWithStoreEvent.class));
        salesmanState = getRuntimeContext().getState(new ValueStateDescriptor<>("salesman-enrichment", SaleWithSalesmanEvent.class));
        expirationState = getRuntimeContext().getState(new ValueStateDescriptor<>("merge-expiration", Long.class));
    }

    @Override
    public void processElement1(SaleWithStoreEvent storeEnriched, Context ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        SaleWithSalesmanEvent salesmanEnriched = salesmanState.value();
        if (salesmanEnriched != null) {
            out.collect(merge(storeEnriched, salesmanEnriched));
            storeState.clear();
            salesmanState.clear();
            expirationState.clear();
        } else {
            storeState.update(storeEnriched);
            long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
            expirationState.update(expiresAt);
            ctx.timerService().registerProcessingTimeTimer(expiresAt);
        }
    }

    @Override
    public void processElement2(SaleWithSalesmanEvent salesmanEnriched, Context ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        SaleWithStoreEvent storeEnriched = storeState.value();
        if (storeEnriched != null) {
            out.collect(merge(storeEnriched, salesmanEnriched));
            storeState.clear();
            salesmanState.clear();
            expirationState.clear();
        } else {
            salesmanState.update(salesmanEnriched);
            long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
            expirationState.update(expiresAt);
            ctx.timerService().registerProcessingTimeTimer(expiresAt);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        SaleWithStoreEvent storeEnriched = storeState.value();
        SaleWithSalesmanEvent salesmanEnriched = salesmanState.value();
        if (storeEnriched == null || salesmanEnriched == null) {
            ExpiredPendingSaleEvent expired = new ExpiredPendingSaleEvent();
            expired.saleId = ctx.getCurrentKey();
            expired.expiresAt = timestamp;
            expired.reason = ExpiredReason.MERGE_TTL_EXPIRED;
            ctx.output(EXPIRED_SALES_TAG, expired);
            storeState.clear();
            salesmanState.clear();
            expirationState.clear();
        }
    }

    private SalesEnrichedEvent merge(SaleWithStoreEvent storeEnriched, SaleWithSalesmanEvent salesmanEnriched) {
        return new SalesEnrichedEvent(
            salesmanEnriched.salesmanId,
            salesmanEnriched.salesmanName,
            storeEnriched.saleId,
            storeEnriched.quantity,
            storeEnriched.productId,
            storeEnriched.storeId,
            storeEnriched.cityName,
            storeEnriched.storeName,
            storeEnriched.saleDate,
            storeEnriched.countryName,
            storeEnriched.amount
        );
    }
}
