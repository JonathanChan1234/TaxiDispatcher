package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpdatedAt implements Serializable {
    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("timezone_type")
    @Expose
    @Ignore
    public Integer timezoneType;
    @SerializedName("timezone")
    @Expose
    @Ignore
    public String timezone;
}
