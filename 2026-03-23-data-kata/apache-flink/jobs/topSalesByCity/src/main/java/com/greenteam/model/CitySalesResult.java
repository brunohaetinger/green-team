package com.greenteam.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/*
 * This class represents the final result of the aggregation of sales data by city.
 * It contains all the relevant information about the sales in a specific city, such as
 * total sales amount, total units sold, and other details that are needed for reporting
 * or further analysis.
 */
public record CitySalesResult(String cityName, LocalDate saleDate, BigDecimal totalAmount, long totalUnits,
                              long sourceEventCount, String windowId, Instant processedAt, long windowStart,
                              long windowEnd) {

    @Override
    public String toString() {
        return "CitySalesResult{" +
                "cityName='" + cityName + '\'' +
                ", saleDate='" + saleDate + '\'' +
                ", totalAmount=" + totalAmount +
                ", totalUnits=" + totalUnits +
                ", windowEnd='" + windowEnd + '\'' +
                ", sourceEventCount=" + sourceEventCount +
                ", windowId='" + windowId + '\'' +
                '}';
    }
}

