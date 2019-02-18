package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.SerializedName;

public class StandardResponse {
    @SerializedName("success")
    public Integer success;
    @SerializedName("message")
    public String message;
}
