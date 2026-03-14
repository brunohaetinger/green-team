package com.greenteam.model;

public class PendingSalesBySalesman {

    public SaleWithStoreEvent sale;
    public long expiresAt;

    public PendingSalesBySalesman() {}

    public PendingSalesBySalesman(SaleWithStoreEvent sale, long expiresAt) {
        this.sale = sale;
        this.expiresAt = expiresAt;
    }
}