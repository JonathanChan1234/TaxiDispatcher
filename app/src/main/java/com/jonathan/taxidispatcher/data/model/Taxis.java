package com.jonathan.taxidispatcher.data.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Taxis {
    @SerializedName("owned_taxis")
    @Expose
    @Nullable
    public List<Taxi> taxis;
}
