package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideShareResource {
    @SerializedName("data")
    @Expose
    public RideShare data;
}
