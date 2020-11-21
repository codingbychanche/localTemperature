package com.example.locationlibrary;

public class EnvironmentAddress {
    /**
     * Creates a new address- instance
     */
    private String address,city,state,postalCode;

    public EnvironmentAddress(String address, String city, String state, String postalCode) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
