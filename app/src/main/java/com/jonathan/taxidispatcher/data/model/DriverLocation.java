package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DriverLocation implements Serializable {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("location")
    @Expose
    public Location location;

    public class Location implements Serializable{
        @SerializedName("latitude")
        @Expose
        public String latitude;

        @SerializedName("longitude")
        @Expose
        public String longitude;

        @SerializedName("timestamp")
        @Expose
        public String timestamp;
    }
}
