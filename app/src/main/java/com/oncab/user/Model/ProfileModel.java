package com.oncab.user.Model;

import com.google.gson.annotations.SerializedName;

public class ProfileModel {
    @SerializedName("status")
    private Integer status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private ProfileData data;

    public Integer getStatus() { return status; }
    public String getMessage() { return message; }
    public ProfileData getData() { return data; }
}
