package com.greenteam.model;

import java.io.Serializable;
/*
    * This class represents a sale event that has been enriched with the salesman information.
    * This class is used as the value type for the state that is used to store the pending sales that are waiting for their corresponding store information
    * The sale with salesman event is created by joining the sales event with the salesman information that is stored in the state.
    * The sale with salesman event is then used as the input for the enrichment process, where we will join it with the store information
 */
public class SaleWithSalesmanEvent implements Serializable {
    public int saleId;
    public int salesmanId;
    public String salesmanName;
    public String saleDate;
    public int productId;
    public int storeId;
    public int quantity;

    public SaleWithSalesmanEvent() {}

    public SaleWithSalesmanEvent(int saleId, int salesmanId, String salesmanName, String saleDate, int productId, int storeId, int quantity) {
        this.saleId = saleId;
        this.salesmanId = salesmanId;
        this.salesmanName = salesmanName;
        this.saleDate = saleDate;
        this.productId = productId;
        this.storeId = storeId;
        this.quantity = quantity;
    }
}

