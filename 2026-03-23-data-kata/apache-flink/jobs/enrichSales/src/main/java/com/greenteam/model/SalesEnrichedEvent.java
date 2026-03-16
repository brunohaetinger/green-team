package com.greenteam.model;

import java.math.BigDecimal;

/*
    * This class represents an enriched sale event that contains all the information about a sale, including the salesman and store information. 
    * It is the final output of the enrichment process and will be emitted to the output topic.s
*/
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