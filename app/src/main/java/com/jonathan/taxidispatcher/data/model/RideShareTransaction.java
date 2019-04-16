package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class RideShareTransaction {
    @SerializedName("id")
    @Expose
    @PrimaryKey
    public Integer id;

    @SerializedName("first_transaction")
    @Expose
    @Embedded(prefix = "firstTransaction_")
    public RideShare first_transaction;

    @SerializedName("second_transaction")
    @Expose
    @Embedded(prefix = "secondTransaction_")
    public RideShare second_transaction;

    @SerializedName("driver")
    @Expose
    @Ignore
    public Driver driver;

    @SerializedName("taxi")
    @Expose
    @Ignore
    public RideShareTaxi taxi;

    @SerializedName("status")
    @Expose
    public Integer status;

    @SerializedName("created_at")
    @Expose
    @Ignore
    public CreatedAt createdAt;

    @SerializedName("updated_at")
    @Expose
    @Ignore
    public UpdatedAt updatedAt;

    @SerializedName("first_confirmed")
    @Expose
    public Integer first_confirmed;

    @SerializedName("second_confirmed")
    @Expose
    public Integer second_confirmed;

    @SerializedName("firstReachTime")
    @Expose
    public String first_reach_time;

    @SerializedName("secondReachTime")
    @Expose
    public String second_reach_time;
}
