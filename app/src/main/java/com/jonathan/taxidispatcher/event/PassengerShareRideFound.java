package com.jonathan.taxidispatcher.event;

import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.Transcation;

public class PassengerShareRideFound {
    public RideShareTransaction transaction;

    public PassengerShareRideFound(RideShareTransaction transaction) {
        this.transaction = transaction;
    }
}
