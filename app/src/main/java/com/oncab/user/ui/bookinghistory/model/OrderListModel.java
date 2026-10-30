package com.oncab.user.ui.bookinghistory.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderListModel {
    @SerializedName("status")
    private Integer status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Data> data;

    public Integer getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Data> getData() { return data; }
}
