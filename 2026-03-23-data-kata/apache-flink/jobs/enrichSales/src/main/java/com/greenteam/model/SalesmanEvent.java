package com.greenteam.model;

/*
    * This class represents a salesman event that is emitted by the salesman source operator. 
    * This class is used as the input for the enrichment process, where we will join it with the sales events that are waiting for their corresponding salesman information
    * The salesman event is deserialized from the JSON messages that are consumed from the salesmans topic in Kafka. 
    * The JSON messages are expected to have the same structure as the SalesmanEvent class, with the same field names and types. 
    * If the JSON messages do not match the structure of the SalesmanEvent class, they will be deserialization errors and will be discarded from the stream.
*/
public class SalesmanEvent {

    public int id;
    public String name;
    public int storeId;

    public SalesmanEvent(int id, String name, int storeId) {
        this.id = id;
        this.name = name;
        this.storeId = storeId;
    }
}