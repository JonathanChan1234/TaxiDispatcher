package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RideShareCollection {
    @SerializedName("data")
    @Expose
    public List<RideShareTransaction> transactionList;
}
