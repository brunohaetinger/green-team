package com.greenteam.reportapi.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class TotalSalesByCityId implements Serializable {

    private String cityName;
    private LocalDate saleDate;

    public TotalSalesByCityId() {}

    public TotalSalesByCityId(String cityName, LocalDate saleDate) {
        this.cityName = cityName;
        this.saleDate = saleDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TotalSalesByCityId that)) return false;
        return Objects.equals(cityName, that.cityName) && Objects.equals(saleDate, that.saleDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityName, saleDate);
    }
}
