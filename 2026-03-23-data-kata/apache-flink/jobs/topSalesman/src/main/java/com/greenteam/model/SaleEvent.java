package com.greenteam.model;

import java.math.BigDecimal;

public class SaleEvent {

    public String eventId;
    public String traceId;
    public int saleId;
    public int salesmanId;
    public String salesmanName;
    public String saleDate;
    public int quantity;
    public BigDecimal amount;

    public SaleEvent(String eventId, String traceId, int saleId, int salesmanId, String salesmanName, String saleDate, int quantity, BigDecimal amount) {
        this.eventId = eventId;
        this.traceId = traceId;
        this.saleId = saleId;
        this.salesmanId = salesmanId;
        this.salesmanName = salesmanName;
        this.saleDate = saleDate;
        this.quantity = quantity;
        this.amount = amount;
    }
}