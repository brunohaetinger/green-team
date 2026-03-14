package com.greenteam.model;

import java.math.BigDecimal;

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