package com.jonathan.taxidispatcher.event.driver;

import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.Transcation;

public class PassengerFoundEvent {
    private String data;

    public PassengerFoundEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
