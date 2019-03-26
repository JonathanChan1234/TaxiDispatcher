package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Ignore;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Taxi {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("plateNumber")
    @Expose
    public String platenumber;
    @SerializedName("last_login_time")
    @Expose
    @Nullable
    public String lastLoginTime;
    @SerializedName("last_logout_time")
    @Expose
    @Nullable
    public String lastLogoutTime;
    @SerializedName("driver_id")
    @Expose
    @Nullable
    @Ignore
    public String driverId;
    @SerializedName("created_at")
    @Expose
    public String createdAt;
    @SerializedName("updated_at")
    @Expose
    public String updatedAt;
    @SerializedName("occupied")
    @Expose
    public Integer occupied;
    @SerializedName("password")
    @Expose
    public String password;
    @SerializedName("owner")
    @Expose
    @Ignore
    public Driver owner;
}