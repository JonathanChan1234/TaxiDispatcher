package com.jonathan.taxidispatcher.data.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DriverFoundResponse {
    @SerializedName("transcation")
    @Expose
    @Nullable
    public Transcation transcation;

    @SerializedName("driver")
    @Expose
    @Nullable
    public Driver driver;
}
