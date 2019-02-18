package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(indices = {@Index("id")}, primaryKeys = {"id"})
public class Transcation implements Serializable {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("user")
    @Expose
    @Ignore
    public User user;
    @SerializedName("driver")
    @Expose
    @Nullable
    @Embedded(prefix = "driver_")
    public Driver driver;
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
    @SerializedName("requirement")
    @Expose
    @Nullable
    public String requirement;
    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("meet_up_time")
    @Expose
    public String meetUpTime;
    @SerializedName("created_at")
    @Expose
    @Embedded(prefix = "created_at")
    public CreatedAt createdAt;
    @SerializedName("updated_at")
    @Expose
    @Embedded(prefix = "updated_at")
    public UpdatedAt updatedAt;
    @SerializedName("taxi")
    @Expose
    @Nullable
    @Embedded(prefix = "taxi_")
    public Taxi taxi;
}
