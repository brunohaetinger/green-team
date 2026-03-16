package com.greenteam.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/* 
 * This class serves as the accumulator for the aggregation of sales data by city.
 * It holds the intermediate results of the aggregation, such as total sales amount,
 * total units sold, and other relevant information needed to compute the final results.
 */
public class CitySalesAccumulator {
    public BigDecimal totalAmount = BigDecimal.ZERO;
    public long totalUnits  = 0;
}

