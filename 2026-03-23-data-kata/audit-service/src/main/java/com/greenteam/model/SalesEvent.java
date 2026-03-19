package com.greenteam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesEvent {

    @JsonProperty("salrty(\"salesman_id\")\n" +
            "    private int salesmanId;esman_id")
    private int salesmanId;

    @JsonProperty("salesman_name")
    private String salesmanName;

    @JsonProperty("sale_id")
    private int saleId;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("sale_date")
    private String saleDate;

    @JsonProperty("product_id")
    private int productId;

    @JsonProperty("store_id")
    private int storeId;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("store_name")
    private String storeName;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("amount")
    private String amount;

    public SalesEvent() {
    }

    public int getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(int salesmanId) {
        this.salesmanId = salesmanId;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String toCsvRow() {
        return String.join(",",
                String.valueOf(salesmanId),
                escapeCsv(salesmanName),
                String.valueOf(saleId),
                String.valueOf(quantity),
                escapeCsv(saleDate),
                String.valueOf(productId),
                String.valueOf(storeId),
                escapeCsv(cityName),
                escapeCsv(storeName),
                escapeCsv(countryName),
                escapeCsv(amount));
    }

    public static String getCsvHeader() {
        return "salesman_id,salesman_name,sale_id,quantity,sale_date,product_id,store_id,city_name,store_name,country_name,amount";
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
