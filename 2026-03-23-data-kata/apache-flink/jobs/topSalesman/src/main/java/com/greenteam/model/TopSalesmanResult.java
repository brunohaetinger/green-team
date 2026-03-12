package com.greenteam.model;

import java.math.BigDecimal;

public class TopSalesmanResult {

    public final String salesmanId;
    public final String countryId;
    public final String windowStart;
    public final String windowEnd;
    public final BigDecimal totalAmount;
    public final long totalUnits;
    public final long totalOrders;
    public final long eventCount;
    public final String processedAt;

    public TopSalesmanResult(
        String salesmanId,
        String countryId,
        String windowStart,
        String windowEnd,
        BigDecimal totalAmount,
        long totalUnits,
        long totalOrders,
        long eventCount,
        String processedAt
    ) {
        this.salesmanId = salesmanId;
        this.countryId = countryId;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.totalAmount = totalAmount;
        this.totalUnits = totalUnits;
        this.totalOrders = totalOrders;
        this.eventCount = eventCount;
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "TopSalesmanResult{" +
            "salesmanId='" + salesmanId + '\'' +
            ", windowEnd='" + windowEnd + '\'' +
            ", totalAmount=" + totalAmount +
            ", totalUnits=" + totalUnits +
            '}';
    }
}