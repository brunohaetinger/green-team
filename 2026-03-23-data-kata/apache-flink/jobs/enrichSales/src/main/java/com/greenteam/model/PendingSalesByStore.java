package com.greenteam.model;

public class PendingSalesByStore {

    public SalesEvent sale;
    public long expiresAt;

    public PendingSalesByStore() {}

    public PendingSalesByStore(SalesEvent sale, long expiresAt) {
        this.sale = sale;
        this.expiresAt = expiresAt;
    }
}