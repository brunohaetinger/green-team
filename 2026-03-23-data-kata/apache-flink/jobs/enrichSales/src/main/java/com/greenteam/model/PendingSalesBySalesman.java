package com.greenteam.model;

/*
    * This class represents a pending sale event that is waiting for its corresponding salesman information
    * to be enriched. It contains the sale event and the expiration time of the pending sale. If the pending sale
    * expires before it can be enriched with the salesman information
    * it will be discarded from the state. The expiration time is calculated based on the current processing time and the TTL defined in the JobConfig.
    * The pending sales are stored in a state that is keyed by the salesman ID, which allows us to efficiently look up the pending sales for a given salesman when we receive a salesman event.
    * When a salesman event is received, we can look up the pending sales for that salesman ID and enrich them with the salesman information before emitting the enriched sale events to the output topic.
*/
public class PendingSalesBySalesman {

    public SaleWithStoreEvent sale;
    public long expiresAt;
    public SalesmanEvent lastKnownSalesman;

    public PendingSalesBySalesman(SaleWithStoreEvent sale, long expiresAt, SalesmanEvent lastKnownSalesman) {
        this.sale = sale;
        this.expiresAt = expiresAt;
        this.lastKnownSalesman = lastKnownSalesman;
    }
}