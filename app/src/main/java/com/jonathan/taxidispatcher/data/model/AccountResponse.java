package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.SerializedName;

public class AccountResponse {
    @SerializedName("success")
    public Integer success;
    @SerializedName("access_token")
    public String access_token;
    @SerializedName("token_type")
    public String token_type;
    @SerializedName("expires_in")
    public Integer expires_in;
    @SerializedName("user")
    public User user;
}
