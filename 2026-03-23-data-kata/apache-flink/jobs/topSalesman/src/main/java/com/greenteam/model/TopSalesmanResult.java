package com.greenteam.model;

import java.math.BigDecimal;

public class TopSalesmanResult {

    public final String salesmanName;
    public final String saleDate;
    public final BigDecimal totalAmount;
    public final long totalUnits;

    public TopSalesmanResult(
        String salesmanName,
        String saleDate,
        BigDecimal totalAmount,
        long totalUnits
    ) {
        this.salesmanName = salesmanName;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.totalUnits = totalUnits;
    }

    @Override
    public String toString() {
        return "TopSalesmanResult{" +
            "salesmanName='" + salesmanName + '\'' +
            ", saleDate='" + saleDate + '\'' +
            ", totalAmount=" + totalAmount +
            ", totalUnits=" + totalUnits +
            '}';
    }
}