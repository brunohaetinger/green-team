package com.greenteam.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class CitySalesAccumulator {

    public BigDecimal totalAmount = BigDecimal.ZERO;
    public long totalUnits  = 0;
    public long eventCount  = 0;
    public String storeName;
    public String saleDate;
    public String cityName;
    public Set<Integer> saleIds = new HashSet<>();
}

