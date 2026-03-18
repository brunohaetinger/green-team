package com.greenteam.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class TopSalesmanAccumulator {
    public int salesmanId = -1;
    public String salesmanName;
    public LocalDate saleDate;
    public BigDecimal totalAmount = BigDecimal.ZERO;
    public long totalUnits = 0;
    public Instant processedAt;
}
