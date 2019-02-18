package com.jonathan.taxidispatcher.data.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AccountUserResponse {
    @SerializedName("success")
    public Integer success;

    @Nullable
    @SerializedName("access_token")
    public String access_token;

    @Nullable
    @SerializedName("token_type")
    public String token_type;

    @Nullable
    @SerializedName("expires_in")
    public Integer expires_in;

    @Nullable
    @SerializedName("user")
    public User user;

    @SerializedName("message")
    public List<String> message;
}
