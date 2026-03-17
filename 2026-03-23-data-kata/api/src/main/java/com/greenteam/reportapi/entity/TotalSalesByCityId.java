package com.greenteam.reportapi.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class TotalSalesByCityId implements Serializable {

    private Integer storeId;
    private LocalDate saleDate;

    public TotalSalesByCityId() {}

    public TotalSalesByCityId(Integer storeId, LocalDate saleDate) {
        this.storeId = storeId;
        this.saleDate = saleDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TotalSalesByCityId that)) return false;
        return Objects.equals(storeId, that.storeId) && Objects.equals(saleDate, that.saleDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, saleDate);
    }
}
