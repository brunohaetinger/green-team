package com.greenteam.model;

import java.math.BigDecimal;

/*
    * This class represents a sale event that has been enriched with the store information. 
    * This class is used as the value type for the state that is used to store the pending sales that are waiting for their corresponding salesman information
    * The sale with store event is created by joining the sales event with the store information that is stored in the state.
    * The sale with store event is then used as the input for the enrichment process, where we will join it with the salesman information
*/
public class SaleWithStoreEvent {

    public int salesmanId;
    public int saleId;
    public int quantity;
    public int productId;
    public int storeId;
    public String cityName;
    public String storeName;
    public String saleDate;
    public String countryName;
    public BigDecimal amount;

    public SaleWithStoreEvent(
        int salesmanId,
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