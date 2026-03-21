package com.greenteam.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TopSalesmanResult(int salesmanId, String salesmanName, LocalDate saleDate, BigDecimal totalAmount,
                                long totalUnits, Instant processedAt, long windowStart, long windowEnd) {

    @Override
    public String toString() {
        return "TopSalesmanResult{" +
                "salesmanId=" + salesmanId +
                ", " +
                "salesmanName='" + salesmanName + '\'' +
                ", saleDate='" + saleDate + '\'' +
                ", totalAmount=" + totalAmount +
                ", totalUnits=" + totalUnits +
                '}';
    }
}