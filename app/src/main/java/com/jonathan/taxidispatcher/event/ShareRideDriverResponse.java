package com.jonathan.taxidispatcher.event;

public class ShareRideDriverResponse {
    public int response;
    public Integer rideShareId;
    public Integer driverId;

    public ShareRideDriverResponse(int i, Integer rideShareId, Integer id) {
        this.response = i;
        this.rideShareId = rideShareId;
        this.driverId = id;
    }
}
