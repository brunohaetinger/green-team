package com.greenteam.model;

import java.math.BigDecimal;

/*
    * This class represents a sales event that is emitted by the sales source operator. 
    * This class is used as the input for the enrichment process, where we will join it with the store and salesman information
    * The enriched sale event will then be emitted to the output topic for further processing or analysis.
    * The sales event is deserialized from the JSON messages that are consumed from the sales topic in Kafka. 
    * The JSON messages are expected to have the same structure as the SalesEvent class, with the same field names and types. 
    * If the JSON messages do not match the structure of the SalesEvent class, they will be deserialization errors and will be discarded from the stream.
    * The sales event is also used as the value type for the state that is used to store the pending sales that are waiting for their corresponding store and salesman information
*/
public class SalesEvent {

    public int saleId;
    public int salesmanId;
    public int storeId;
    public BigDecimal amount;
    public String saleDate;
    public int productId;
    public int quantity;

    public SalesEvent() {}

    public SalesEvent(int saleId, int salesmanId, int storeId, BigDecimal amount, String saleDate, int productId, int quantity) {
        this.saleId = saleId;
        this.salesmanId = salesmanId;
        this.storeId = storeId;
        this.amount = amount;
        this.saleDate = saleDate;
        this.productId = productId;
        this.quantity = quantity;
    }
}