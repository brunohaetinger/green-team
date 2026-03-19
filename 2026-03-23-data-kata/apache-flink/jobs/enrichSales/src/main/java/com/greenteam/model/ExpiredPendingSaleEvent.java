package com.greenteam.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Event representing a sale that expired while waiting for enrichment (salesman, store, etc).
 * Can be used to persist in Kafka and reprocess later.
 */
public class ExpiredPendingSaleEvent implements Serializable {
    public String eventId;
    public String traceId;
    public int saleId;
    public int salesmanId;
    public String salesmanName;
    public String saleDate;
    public int productId;
    public int storeId;
    public String cityName;
    public String storeName;
    public String countryName;
    public BigDecimal amount;
    public int quantity;
    public long expiresAt;
    public ExpiredReason reason;

    public ExpiredPendingSaleEvent() {
    }

    public ExpiredPendingSaleEvent(
            int saleId,
            String eventId,
            String traceId,
            int salesmanId,
            String salesmanName,
            String saleDate,
            int productId,
            int storeId,
            String cityName,
            String storeName,
            String countryName,
            BigDecimal amount,
            int quantity,
            long expiresAt,
            ExpiredReason reason
    ) {
        this.saleId = saleId;
        this.eventId = eventId;
        this.traceId = traceId;
        this.salesmanId = salesmanId;
        this.salesmanName = salesmanName;
        this.saleDate = saleDate;
        this.productId = productId;
        this.storeId = storeId;
        this.cityName = cityName;
        this.storeName = storeName;
        this.countryName = countryName;
        this.amount = amount;
        this.quantity = quantity;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    public static ExpiredPendingSaleEvent fromPending(PendingSalesBySalesman pending, ExpiredReason reason) {
        SaleWithStoreEvent sale = pending.sale;
        String salesmanName = null;
        if (pending.lastKnownSalesman != null) {
            salesmanName = pending.lastKnownSalesman.name;
        }
        return new ExpiredPendingSaleEvent(
                sale.saleId,
                sale.eventId,
                sale.traceId,
                sale.salesmanId,
                salesmanName,
                sale.saleDate,
                sale.productId,
                sale.storeId,
                sale.cityName,
                sale.storeName,
                sale.countryName,
                sale.amount,
                sale.quantity,
                pending.expiresAt,
                reason
        );
    }

    public static ExpiredPendingSaleEvent fromPending(PendingSalesByStore pending, ExpiredReason reason) {
        SalesEvent sale = pending.sale;
        String cityName = null;
        String storeName = null;
        String countryName = null;
        if (pending.lastKnownStore != null) {
            cityName = pending.lastKnownStore.city;
            storeName = pending.lastKnownStore.name;
            countryName = pending.lastKnownStore.country;
        }
        return new ExpiredPendingSaleEvent(
                sale.saleId,
                sale.eventId,
                sale.traceId,
                sale.salesmanId,
                null,
                sale.saleDate,
                sale.productId,
                sale.storeId,
                cityName,
                storeName,
                countryName,
                sale.amount,
                sale.quantity,
                pending.expiresAt,
                reason
        );
    }
}
