package com.greenteam.model;

import java.math.BigDecimal;

public class TopSalesmanResult {

    public final int salesmanId;
    public final String salesmanName;
    public final String saleDate;
    public final BigDecimal totalAmount;
    public final long totalUnits;

    public TopSalesmanResult(
        int salesmanId,
        String salesmanName,
        String saleDate,
        BigDecimal totalAmount,
        long totalUnits
    ) {
        this.salesmanId = salesmanId;
        this.salesmanName = salesmanName;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.totalUnits = totalUnits;
    }

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