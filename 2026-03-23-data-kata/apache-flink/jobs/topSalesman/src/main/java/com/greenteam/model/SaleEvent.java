package com.greenteam.model;

import java.math.BigDecimal;

public class SaleEvent {

    public int salesmanId;
    public String salesmanName;
    public String saleDate;
    public int quantity;
    public BigDecimal amount;

    public SaleEvent(int salesmanId, String salesmanName, String saleDate, int quantity, BigDecimal amount) {
        this.salesmanId = salesmanId;
        this.salesmanName = salesmanName;
        this.saleDate = saleDate;
        this.quantity = quantity;
        this.amount = amount;
    }
}