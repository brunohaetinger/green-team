package com.greenteam.operator;

import com.greenteam.model.PendingSalesBySalesman;
import com.greenteam.model.SaleWithStoreEvent;
import com.greenteam.model.SalesEnrichedEvent;
import com.greenteam.model.SalesmanEvent;
import com.greenteam.model.ExpiredPendingSaleEvent;
import com.greenteam.model.ExpiredReason;
import org.apache.flink.util.OutputTag;
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
    * This class joins the sales events that have been enriched with the store information with the salesman information
    * The join is performed by using a keyed state that stores the salesman information
    * When a sale event is received, we check if the corresponding salesman information
    * is already stored in the state. If it is, we can immediately enrich the sale event with the salesman information
*/
public class JoinSalesWithSalesman extends KeyedCoProcessFunction<Integer, SaleWithStoreEvent, SalesmanEvent, SalesEnrichedEvent> {

    private final long ttlMs;

    private transient ValueState<SalesmanEvent> salesmanState;
    private transient MapState<Integer, PendingSalesBySalesman> pendingState;

    private transient Counter pendingCounter;
    private transient Counter lateJoinCounter;
    private transient Counter ttlExpiredCounter;
    private transient AtomicLong pendingGaugeValue;

    // OutputTag for side output of expired sales
    public static final OutputTag<ExpiredPendingSaleEvent> EXPIRED_SALES_TAG =
            new OutputTag<>("expired-sales"){};

    public JoinSalesWithSalesman(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    // The open method is called when the operator is initialized. It is used to set up the state and metrics that will be used by the operator.
    // We create a ValueState to store the salesman information and a MapState to store the pending sales that are waiting for their corresponding salesman information
    // We also create three counters to track the number of pending sales, the number of late joins, and the number of TTL expirations.
    // Finally, we create a gauge to track the current number of pending sales in the state, which is updated whenever we add or remove pending sales from the state.
    @Override
    public void open(OpenContext openContext) {
        salesmanState = getRuntimeContext().getState(new ValueStateDescriptor<>("salesman-state", SalesmanEvent.class));
        pendingState = getRuntimeContext().getMapState(
            // The MapState is keyed by the sale ID, which allows us to efficiently look up the pending sales for a given sale when we receive a salesman event.
            new MapStateDescriptor<>("pending-sales-by-salesman", Integer.class, PendingSalesBySalesman.class)
        );

        // The pending counter is incremented whenever we add a new pending sale to the state, and decremented whenever we remove a pending sale from the state (either because it was enriched with the salesman information or because it expired).
        pendingCounter = getRuntimeContext().getMetricGroup().counter("pending_sales_missing_salesman_total");
        // The late join counter is incremented whenever we successfully enrich a pending sale with the salesman information after it has been added to the state, which indicates that the salesman information arrived after the sale event.
        lateJoinCounter = getRuntimeContext().getMetricGroup().counter("late_salesman_join_total");
        // The TTL expired counter is incremented whenever a pending sale expires from the state before it can be enriched with the salesman information, which indicates that the salesman information did not arrive within the TTL defined in the JobConfig.
        ttlExpiredCounter = getRuntimeContext().getMetricGroup().counter("pending_sales_salesman_ttl_expired_total");

        // The pending gauge is updated whenever we add or remove pending sales from the state, which allows us to track the current number of pending sales in the state at any given time.    
        pendingGaugeValue = new AtomicLong(0L);
        // The gauge is registered with the metric group and will call the get method of the AtomicLong to retrieve the current value of the gauge whenever it is queried.
        getRuntimeContext().getMetricGroup().gauge("pending_sales_missing_salesman_current", pendingGaugeValue::get);
    }

    // The processElement1 method is called whenever a new sale event is received.
    // It checks if the corresponding salesman information is already stored in the state.
    // If it is, it enriches the sale event with the salesman information
    @Override
    public void processElement1(SaleWithStoreEvent sale, Context ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        SalesmanEvent salesman = salesmanState.value();
        if (salesman != null) {
            out.collect(merge(sale, salesman));
            return;
        }

        long expiresAt = ctx.timerService().currentProcessingTime() + ttlMs;
        pendingState.put(sale.saleId, new PendingSalesBySalesman(sale, expiresAt, null));
        ctx.timerService().registerProcessingTimeTimer(expiresAt);
        pendingCounter.inc();
        pendingGaugeValue.incrementAndGet();
    }

    // The processElement2 method is called whenever a new salesman event is received. 
    // It updates the salesman information in the state and checks if there are any pending sales that can be enriched with the new salesman information.
    // We iterate over the pending sales in the MapState and check if any of them can be enriched with the new salesman information.
    // If a pending sale can be enriched, we emit the enriched sale event to the output topic and remove the pending sale from the state. 
    // If a pending sale has expired, we remove it from the state and increment the TTL expired counter. 
    // If a pending sale is enriched with the salesman information, we increment the late join counter, which indicates that the salesman information arrived after the sale event, but we were still able to successfully enrich the sale event with the salesman information.
    @Override
    public void processElement2(SalesmanEvent salesman, Context ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        salesmanState.update(salesman);

        Iterator<Map.Entry<Integer, PendingSalesBySalesman>> iterator = pendingState.entries().iterator();
        long now = ctx.timerService().currentProcessingTime();

        while (iterator.hasNext()) {
            Map.Entry<Integer, PendingSalesBySalesman> entry = iterator.next();
            PendingSalesBySalesman pending = entry.getValue();

            if (pending.sale.salesmanId == salesman.id) {
                pending.lastKnownSalesman = salesman;
            }

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

    // The onTimer method is called whenever a timer that we registered for a pending sale expires.
    // We iterate over the pending sales in the MapState and check if any of them have expired. 
    // If a pending sale has expired, we remove it from the state and increment the TTL expired
    // counter, which indicates that the salesman information did not arrive within the TTL defined in the JobConfig and the pending sale was discarded from the state.
    // This method is important for cleaning up expired pending sales from the state and preventing the state from growing indefinitely with pending sales that will never be enriched with the salesman information
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<SalesEnrichedEvent> out) throws Exception {
        Iterator<Map.Entry<Integer, PendingSalesBySalesman>> iterator = pendingState.entries().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, PendingSalesBySalesman> entry = iterator.next();
            PendingSalesBySalesman pending = entry.getValue();
            if (pending.expiresAt <= timestamp) {
                // Emit expired event to side output
                ctx.output(EXPIRED_SALES_TAG, ExpiredPendingSaleEvent.fromPending(pending, ExpiredReason.SALESMAN_TTL_EXPIRED));
                iterator.remove();
                pendingGaugeValue.decrementAndGet();
                ttlExpiredCounter.inc();
            }
        }
    }

    // The merge method is a helper method that takes a sale event that has been enriched with the store information and a salesman event, 
    // and merges them into a single SalesEnrichedEvent that contains all the information from both events.
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