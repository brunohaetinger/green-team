package com.greenteam.model;

import java.math.BigDecimal;

/* 
 * This class represents a single sale event, containing all the relevant information about the sale,
 * such as the city where the sale occurred, the store details, the date of the sale, the quantity
 * sold, and the total amount of the sale. This class is used as the input data for processing and
 * aggregation in the Flink job.
 */
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

