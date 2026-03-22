package com.greenteam.reportapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TopSalesmanDto(
        String salesmanName,
        String cityName,
        String countryName,
        LocalDate saleDate,
        BigDecimal totalAmount,
        Long totalUnits
) {}
