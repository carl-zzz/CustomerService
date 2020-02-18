package com.app.model;


// Model of a typical customer
public class Customer {

    // Id of the customer
    private long id;

    // Phone number of the customer
    private String phoneNumber;

    // The estimated time of the call
    private long callDuration;

    // The level of help that the customer may need
    private long helpLevel;

    // Constructor
    public Customer(long id, String phoneNumber, long callDuration, long helpLevel) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.callDuration = callDuration;
        this.helpLevel = helpLevel;
    }

    public long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getCallDuration() {
        return callDuration;
    }

    public long getHelpLevel() {
        return helpLevel;
    }
}
