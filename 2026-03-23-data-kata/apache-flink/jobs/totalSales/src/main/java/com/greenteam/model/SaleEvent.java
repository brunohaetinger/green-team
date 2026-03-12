package com.greenteam.model;

import java.math.BigDecimal;

public class SaleEvent {

    public String cityId;
    public String countryId;
    public String saleId;
    public int quantity;
    public BigDecimal amount;

    public SaleEvent() {}

    public SaleEvent(String cityId, String countryId, String saleId, int quantity, BigDecimal amount) {
        this.cityId = cityId;
        this.countryId = countryId;
        this.saleId = saleId;
        this.quantity = quantity;
        this.amount = amount;
    }
}

