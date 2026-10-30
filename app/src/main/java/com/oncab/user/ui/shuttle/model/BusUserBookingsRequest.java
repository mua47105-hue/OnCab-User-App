package com.oncab.user.ui.shuttle.model;

import com.google.gson.annotations.SerializedName;

/** Request body for BUS user-bookings endpoint. */
public class BusUserBookingsRequest {
    @SerializedName("passenger_mobile")
    private String passengerMobile;
    @SerializedName("passenger_id")
    private String passengerId;
    @SerializedName("status")
    private String status;

    public BusUserBookingsRequest(String passengerMobile, String passengerId, String status) {
        this.passengerMobile = passengerMobile;
        this.passengerId = passengerId;
        this.status = status;
    }

    public String getPassengerMobile() { return passengerMobile; }
    public String getPassengerId() { return passengerId; }
    public String getStatus() { return status; }
}
