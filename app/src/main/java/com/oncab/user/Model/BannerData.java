package com.oncab.user.Model;

import com.google.gson.annotations.SerializedName;

public class BannerData {
    @SerializedName("id")
    private Integer id;
    @SerializedName("title")
    private String title;
    @SerializedName("desc")
    private String desc;
    @SerializedName("img")
    private String img;
    @SerializedName("status")
    private String status;
    @SerializedName("size")
    private BannerSize size;

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public String getImg() { return img; }
    public String getStatus() { return status; }
    public BannerSize getSize() { return size; }
}
