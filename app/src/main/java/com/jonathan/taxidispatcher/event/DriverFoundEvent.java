package com.jonathan.taxidispatcher.event;

import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.Transcation;

public class DriverFoundEvent {
    private Transcation transcation;
    private Driver driver;

    public DriverFoundEvent(Transcation transcation, Driver driver) {
        this.driver = driver;
        this.transcation = transcation;
    }

    public Driver getDriver() {
        return driver;
    }

    public Transcation getTranscation() {
        return transcation;
    }
}
