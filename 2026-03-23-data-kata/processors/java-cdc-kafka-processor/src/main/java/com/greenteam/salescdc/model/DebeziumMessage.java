package com.greenteam.salescdc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record DebeziumMessage(
        @JsonProperty("payload") Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            @JsonProperty("after") RecordData after
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RecordData(
            @JsonProperty("id") Long id,
            @JsonProperty("salesman_id") Integer salesmanId,
            @JsonProperty("product_id") Integer productId,
            @JsonProperty("quantity") Integer quantity
    ) {}
}
