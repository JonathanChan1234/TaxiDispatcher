package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Ignore;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Driver implements Serializable {
    @SerializedName("id")
    @Expose
    public Integer id;

    @SerializedName("phonenumber")
    @Expose
    public String phonenumber;

    @SerializedName("username")
    @Expose
    public String username;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("lat")
    @Expose
    @Nullable
    @Ignore
    public String latitude;

    @SerializedName("long")
    @Expose
    @Nullable
    @Ignore
    public String longitude;

    @SerializedName("occupied")
    @Expose
    @Ignore
    public Integer occupied;

    @SerializedName("location_updated")
    @Expose
    @Ignore
    public String location_updated;

    @SerializedName("updated_at")
    @Expose
    @Ignore
    public String updated_at;

    @SerializedName("rating")
    @Expose
    @Nullable
    @Ignore
    List<Rating> rating;

}
