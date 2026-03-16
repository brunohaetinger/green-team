package com.greenteam.model;

import java.math.BigDecimal;

/* 
 * This class represents the final result of the aggregation of sales data by city.
 * It contains all the relevant information about the sales in a specific city, such as
 * total sales amount, total units sold, and other details that are needed for reporting
 * or further analysis.
 */
public class CitySalesResult {

    public final String cityName;
    public final String saleDate;
    public final BigDecimal totalAmount;
    public final long totalUnits;
    public final String processedAt;
    public final String windowEnd;

    public CitySalesResult(
        String cityName,
        String saleDate,
        BigDecimal totalAmount,
        long totalUnits,
        String processedAt,
        String windowEnd
    ) {
        this.cityName = cityName;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.totalUnits = totalUnits;
        this.processedAt = processedAt;
        this.windowEnd = windowEnd;
    }

    @Override
    public String toString() {
        return "CitySalesResult{" +
            "cityName='" + cityName + '\'' +
            ", saleDate='" + saleDate + '\'' +
            ", totalAmount=" + totalAmount +
            ", totalUnits=" + totalUnits +
            ", windowEnd='" + windowEnd + '\'' +
            '}';
    }
}

