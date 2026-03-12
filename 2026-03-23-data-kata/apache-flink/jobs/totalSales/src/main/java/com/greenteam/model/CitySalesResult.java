package com.greenteam.model;

public class CitySalesResult {

    public final String cityId;
    public final String windowEnd;
    public final String payload;

    public CitySalesResult(String cityId, String windowEnd, String payload) {
        this.cityId    = cityId;
        this.windowEnd = windowEnd;
        this.payload   = payload;
    }

    @Override
    public String toString() {
        return payload;
    }
}

