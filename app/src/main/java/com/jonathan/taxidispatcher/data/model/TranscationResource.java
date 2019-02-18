package com.jonathan.taxidispatcher.data.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TranscationResource implements Serializable {
    @SerializedName("data")
    @Expose
    public Transcation data;
}
