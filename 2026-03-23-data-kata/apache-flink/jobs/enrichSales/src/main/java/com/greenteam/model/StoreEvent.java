package com.greenteam.model;

public class StoreEvent {

    public int id;
    public String name;
    public String city;
    public String state;
    public String country;

    public StoreEvent() {}

    public StoreEvent(int id, String name, String city, String state, String country) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.country = country;
    }
}