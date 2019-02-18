package com.jonathan.taxidispatcher.event;


import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.Transcation;

public class DriverResponseEvent {
    private Transcation transcation;
    private Driver driver;
    private int response;

    public DriverResponseEvent(Transcation transcation, Driver driver, int response) {
        this.driver = driver;
        this.transcation = transcation;
        this.response = response;
    }

    public Driver getDriver() {
        return driver;
    }

    public Transcation getTranscation() {
        return transcation;
    }

    public int getResponse() {
        return response;
    }
}
