package com.jonathan.taxidispatcher.event.driver;

import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.Transcation;

public class DriverShareRideFoundEvent {
    public RideShareTransaction transcation;
    public DriverShareRideFoundEvent(RideShareTransaction transcation) {
        this.transcation = transcation;
    }
}
