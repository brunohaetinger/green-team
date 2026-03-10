package com.greenteam.salescdc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesData(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name
) {}
