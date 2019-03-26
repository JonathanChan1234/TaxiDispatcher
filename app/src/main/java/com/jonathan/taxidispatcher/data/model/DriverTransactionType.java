package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DriverTransactionType {
    @SerializedName("success")
    @Expose
    public Integer success;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("id")
    @Expose
    public Integer transactionId;
    @SerializedName("occupied")
    @Expose
    public Integer occupied;
}
