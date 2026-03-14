package com.greenteam.model;

public class SalesmanEvent {

    public int id;
    public String name;
    public int storeId;

    public SalesmanEvent() {}

    public SalesmanEvent(int id, String name, int storeId) {
        this.id = id;
        this.name = name;
        this.storeId = storeId;
    }
}