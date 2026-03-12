package com.greenteam.salescdc.kafka;

import com.greenteam.salescdc.model.SalesData;

public interface SalesEventPublisher {
    void publish(SalesData salesData);
}
