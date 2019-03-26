package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(indices = {@Index("id")}, primaryKeys = {"id"})
public class RideShareTransaction {
    @SerializedName("id")
    @Expose
    public Integer id;

    @SerializedName("first_transaction")
    @Expose
    @Embedded(prefix = "first_transaction_")
    public RideShare first_transaction;

    @SerializedName("second_transaction")
    @Expose
    @Embedded(prefix = "second_transaction_")
    public RideShare second_transaction;

    @SerializedName("driver")
    @Expose
    @Embedded(prefix = "driver_")
    public Driver driver;

    @SerializedName("taxi")
    @Expose
    @Embedded(prefix = "taxi_")
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
}
