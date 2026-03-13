package com.greenteam.model;

import java.math.BigDecimal;

public class SaleEvent {

    public String cityName;
    public int storeId;
    public String storeName;
    public String saleDate;
    public int saleId;
    public int quantity;
    public BigDecimal amount;

    public SaleEvent() {}

    public SaleEvent(String cityName, int storeId, String storeName, String saleDate, int saleId, int quantity, BigDecimal amount) {
        this.cityName = cityName;
        this.storeId = storeId;
        this.storeName = storeName;
        this.saleDate = saleDate;
        this.saleId = saleId;
        this.quantity = quantity;
        this.amount = amount;
    }
}

