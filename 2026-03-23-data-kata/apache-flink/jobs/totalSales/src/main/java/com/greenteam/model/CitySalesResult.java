package com.greenteam.model;

import java.math.BigDecimal;

public class CitySalesResult {

    public final String cityId;
    public final String countryId;
    public final String windowStart;
    public final String windowEnd;
    public final BigDecimal totalAmount;
    public final long totalUnits;
    public final long totalOrders;
    public final long eventCount;
    public final String processedAt;

    public CitySalesResult(
        String cityId,
        String countryId,
        String windowStart,
        String windowEnd,
        BigDecimal totalAmount,
        long totalUnits,
        long totalOrders,
        long eventCount,
        String processedAt
    ) {
        this.cityId = cityId;
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
        return "CitySalesResult{" +
            "cityId='" + cityId + '\'' +
            ", windowEnd='" + windowEnd + '\'' +
            ", totalAmount=" + totalAmount +
            ", totalUnits=" + totalUnits +
            '}';
    }
}

