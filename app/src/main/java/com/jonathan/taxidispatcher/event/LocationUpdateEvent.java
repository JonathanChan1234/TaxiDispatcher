package com.jonathan.taxidispatcher.event;


import com.jonathan.taxidispatcher.data.model.DriverLocation;

public class LocationUpdateEvent {
    private DriverLocation location;
    public LocationUpdateEvent(DriverLocation location) {
        this.location = location;
    }

    public DriverLocation getLocation() {
        return location;
    }
}
