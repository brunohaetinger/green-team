package com.greenteam.model;

/*
    * This class represents a store event that is emitted by the store source operator. 
    * This class is used as the input for the enrichment process, where we will join it with the sales events that are waiting for their corresponding store information
    * The store event is deserialized from the JSON messages that are consumed from the stores topic in Kafka. 
    * The JSON messages are expected to have the same structure as the StoreEvent class, with the same field names and types. 
    * If the JSON messages do not match the structure of the StoreEvent class, they will be deserialization errors and will be discarded from the stream.
*/
public class StoreEvent {

    public int id;
    public String name;
    public String city;
    public String state;
    public String country;

    public StoreEvent(int id, String name, String city, String state, String country) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.country = country;
    }
}