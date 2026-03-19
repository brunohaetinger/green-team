package com.greenteam.model;

import java.math.BigDecimal;

public class TopSalesmanAccumulator {
    public int salesmanId = -1;
    public String salesmanName;
    public String saleDate;
    public BigDecimal totalAmount = BigDecimal.ZERO;
    public long totalUnits = 0;
    public long sourceEventCount = 0;
}
