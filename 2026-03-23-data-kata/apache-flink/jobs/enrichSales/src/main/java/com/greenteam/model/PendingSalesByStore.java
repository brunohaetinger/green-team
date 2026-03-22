package com.greenteam.model;

/*
    * This class represents a pending sale event that is waiting for its corresponding store information
    * to be enriched. It contains the sale event and the expiration time of the pending sale. If the pending sale
    * expires before it can be enriched with the store information
    * it will be discarded from the state. The expiration time is calculated based on the current processing time and the TTL defined in the JobConfig.
    * The pending sales are stored in a state that is keyed by the store ID, which allows us to efficiently look up the pending sales for a given store when we receive a store event.
    * When a store event is received, we can look up the pending sales for that store ID and enrich them with the store information before emitting the enriched sale events to the output topic.
*/
public class PendingSalesByStore {

    public SalesEvent sale;
    public long expiresAt;
    public StoreEvent lastKnownStore;

    public PendingSalesByStore(SalesEvent sale, long expiresAt, StoreEvent lastKnownStore) {
        this.sale = sale;
        this.expiresAt = expiresAt;
        this.lastKnownStore = lastKnownStore;
    }
}