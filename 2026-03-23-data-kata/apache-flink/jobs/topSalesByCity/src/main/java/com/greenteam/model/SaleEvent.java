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
    public String saleDate;
    public int quantity;
    public BigDecimal amount;

    public SaleEvent(String cityName, String saleDate, int quantity, BigDecimal amount) {
        this.cityName = cityName;
        this.saleDate = saleDate;
        this.quantity = quantity;
        this.amount = amount;
    }
}

