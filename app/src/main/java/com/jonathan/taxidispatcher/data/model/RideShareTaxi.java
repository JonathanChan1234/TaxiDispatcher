package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Ignore;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideShareTaxi {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("platenumber")
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
    @Ignore
    public String createdAt;
    @SerializedName("updated_at")
    @Expose
    @Ignore
    public String updatedAt;
    @SerializedName("occupied")
    @Expose
    @Ignore
    public Integer occupied;
    @SerializedName("password")
    @Expose
    @Ignore
    public String password;
    @SerializedName("owner")
    @Expose
    @Ignore
    public Integer owner;
}
