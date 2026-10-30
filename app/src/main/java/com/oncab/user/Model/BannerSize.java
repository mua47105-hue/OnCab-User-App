package com.oncab.user.Model;

import com.google.gson.annotations.SerializedName;

public class BannerSize {
    @SerializedName("width")
    private Integer width;
    @SerializedName("height")
    private Integer height;

    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
}
