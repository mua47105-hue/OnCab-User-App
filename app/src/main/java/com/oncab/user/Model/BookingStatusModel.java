package com.oncab.user.Model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class BookingStatusModel {
    @SerializedName("status")
    private Integer status;
    @SerializedName("message")
    private JsonElement message;
    @SerializedName("data")
    private JsonElement data;

    public Integer getStatus() { return status; }
    public JsonElement getMessage() { return message; }
    public JsonElement getData() { return data; }
}
