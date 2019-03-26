package com.jonathan.taxidispatcher.data.model;

import android.arch.persistence.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("phonenumber")
    @Expose
    public String phonenumber;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("email_verified_at")
    @Expose
    @Ignore
    public String emailVerifiedAt;
    @SerializedName("transcation_id")
    @Expose
    public Integer transcationId;
    @SerializedName("created_at")
    @Expose
    @Ignore
    public String createdAt;
    @SerializedName("updated_at")
    @Expose
    @Ignore
    public String updatedAt;
}
