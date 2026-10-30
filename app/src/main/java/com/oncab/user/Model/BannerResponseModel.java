package com.oncab.user.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BannerResponseModel {
    @SerializedName("status")
    private Integer status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<BannerData> data;

    public Integer getStatus() { return status; }
    public String getMessage() { return message; }
    public List<BannerData> getData() { return data; }
}
