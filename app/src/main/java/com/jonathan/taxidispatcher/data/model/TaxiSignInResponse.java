package com.jonathan.taxidispatcher.data.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TaxiSignInResponse {
    @SerializedName("success")
    @Expose
    public Integer success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("taxi")
    @Expose
    @Nullable
    public Taxi taxi;
}
