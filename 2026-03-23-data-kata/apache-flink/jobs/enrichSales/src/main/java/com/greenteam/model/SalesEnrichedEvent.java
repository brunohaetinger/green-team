package com.greenteam.model;

import java.math.BigDecimal;

public class SalesEnrichedEvent {

    public int salesmanId;
    public String salesmanName;
    public int saleId;
    public int quantity;
    public int productId;
    public int storeId;
    public String cityName;
    public String storeName;
    public String saleDate;
    public String countryName;
    public BigDecimal amount;

    public SalesEnrichedEvent() {}

    public SalesEnrichedEvent(
        int salesmanId,
        String salesmanName,
        int saleId,
        int quantity,
        int productId,
        int storeId,
        String cityName,
        String storeName,
        String saleDate,
        String countryName,
        BigDecimal amount
    ) {
        this.salesmanId = salesmanId;
        this.salesmanName = salesmanName;
        this.saleId = saleId;
        this.quantity = quantity;
        this.productId = productId;
        this.storeId = storeId;
        this.cityName = cityName;
        this.storeName = storeName;
        this.saleDate = saleDate;
        this.countryName = countryName;
        this.amount = amount;
    }
}