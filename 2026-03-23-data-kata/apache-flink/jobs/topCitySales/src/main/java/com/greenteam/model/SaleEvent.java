package com.greenteam.model;

import java.math.BigDecimal;

public class SaleEvent {

    public String cityName;
    public String storeName;
    public String saleDate;
    public String countryId;
    public String saleId;
    public int quantity;
    public BigDecimal amount;

    public SaleEvent() {}

    public SaleEvent(String cityName, String storeName, String saleDate, String countryId, String saleId, int quantity, BigDecimal amount) {
        this.cityName = cityName;
        this.storeName = storeName;
        this.saleDate = saleDate;
        this.countryId = countryId;
        this.saleId = saleId;
        this.quantity = quantity;
        this.amount = amount;
    }
}

