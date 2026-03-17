package com.greenteam.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Event representing a sale that expired while waiting for enrichment (salesman, store, etc).
 * Can be used to persist in Kafka and reprocess later.
 */

/**
 * Event representing a sale that expired while waiting for salesman information.
 * Can be used to persist in Kafka and reprocess later.
 */
public class ExpiredPendingSaleEvent implements Serializable {
    public int saleId;
    public int salesmanId;
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

    public ExpiredPendingSaleEvent() {}

    public ExpiredPendingSaleEvent(
            int saleId,
            int salesmanId,
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
        this.salesmanId = salesmanId;
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
        return new ExpiredPendingSaleEvent(
                sale.saleId,
                sale.salesmanId,
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
        return new ExpiredPendingSaleEvent(
                sale.saleId,
                sale.salesmanId,
                sale.saleDate,
                sale.productId,
                sale.storeId,
                null, // cityName (not available in SalesEvent)
                null, // storeName (not available in SalesEvent)
                null, // countryName (not available in SalesEvent)
                sale.amount,
                sale.quantity,
                pending.expiresAt,
                reason
        );
    }
}
