package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.jonathan.taxidispatcher.event.Location;

import java.io.Serializable;

public class DriverLocation implements Serializable {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("location")
    @Expose
    public Location location;
}
