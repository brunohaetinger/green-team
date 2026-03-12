package com.greenteam.salescdc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesData(
        @JsonProperty("id") Long id,
        @JsonProperty("salesman_id") Integer salesmanId,
        @JsonProperty("product_id") Integer productId,
        @JsonProperty("quantity") Integer quantity
) {}
