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

    //TODO review datacd
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RecordData(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {}
}
