package com.oncab.user.ui.shuttle.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BusUserBookingsResponse {
    @SerializedName("success")
    private Boolean success;
    @SerializedName("status")
    private Integer status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private BusBookingsData data;

    public Boolean getSuccess() { return success; }
    public Integer getStatus() { return status; }
    public String getMessage() { return message; }
    public BusBookingsData getData() { return data; }

    public static class BusBookingsData {
        @SerializedName("bookings")
        private List<BusBookingData> bookings;

        public List<BusBookingData> getBookings() { return bookings; }
    }
}
