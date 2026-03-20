
package com.greenteam.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SaleEnriched {
    public int salesmanId;
    public String salesmanName;
    public int saleId;
    public int quantity;
    public int productId;
    public Integer storeId; // nullable
    public String cityName;
    public String storeName;
    public LocalDate saleDate;
    public String countryName;
    public BigDecimal amount;

    public SaleEnriched(int salesmanId, String salesmanName, int saleId, int quantity, int productId, Integer storeId,
                        String cityName, String storeName, LocalDate saleDate, String countryName, BigDecimal amount) {
        this.salesmanId = salesmanId;
        this.salesmanName = salesmanName;
        this.saleId = saleId;
        this.quantity = quantity;
        this.productId = productId;
        this.storeId = storeId;
        this.cityName = cityName;
        this.storeName = storeName;
        this.saleDate = saleDate;
        this.countryName = countryName;
        this.amount = amount;
    }
}
