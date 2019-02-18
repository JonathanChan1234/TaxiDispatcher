package com.jonathan.taxidispatcher.data.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AccountDriverResponse {
    @SerializedName("success")
    public Integer success;

    @SerializedName("access_token")
    @Nullable
    public String access_token;

    @SerializedName("token_type")
    @Nullable
    public String token_type;

    @SerializedName("expires_in")
    @Nullable
    public Integer expires_in;

    @SerializedName("user")
    @Nullable
    public Driver user;

    @SerializedName("message")
    public List<String> message;
}
