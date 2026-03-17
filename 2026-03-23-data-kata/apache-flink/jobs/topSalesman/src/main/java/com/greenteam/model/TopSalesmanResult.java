package com.greenteam.model;

import java.math.BigDecimal;

public record TopSalesmanResult(int salesmanId, String salesmanName, String saleDate, BigDecimal totalAmount,
                                long totalUnits) {

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