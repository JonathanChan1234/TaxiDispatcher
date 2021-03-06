package com.jonathan.taxidispatcher.data.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideShare {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("user")
    @Expose
    public User user;
    @SerializedName("rideshare_id")
    @Expose
    public Integer rideshare_id;
    @SerializedName("start_lat")
    @Expose
    public String startLat;
    @SerializedName("start_long")
    @Expose
    public String startLong;
    @SerializedName("start_addr")
    @Expose
    public String startAddr;
    @SerializedName("des_lat")
    @Expose
    public String desLat;
    @SerializedName("des_long")
    @Expose
    public String desLong;
    @SerializedName("des_addr")
    @Expose
    public String desAddr;
    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("created_at")
    @Expose
    public String createdAt;
    @SerializedName("updated_at")
    @Expose
    public String updatedAt;
    @SerializedName("driverReachTime")
    @Expose
    @Nullable
    public String driverReachTime;
}
