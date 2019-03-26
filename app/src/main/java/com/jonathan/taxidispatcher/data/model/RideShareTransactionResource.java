package com.jonathan.taxidispatcher.data.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideShareTransactionResource {
    @SerializedName("data")
    @Expose
    public RideShareTransaction data;
}
