package com.oncab.user.ui.bookinghistory.model;

import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("source")
    private String source;
    @SerializedName("destination")
    private String destination;
    @SerializedName("amount")
    private String amount;
    @SerializedName("fare")
    private String fare;
    @SerializedName("total_fare")
    private String totalFare;
    @SerializedName("payable_amount")
    private String payableAmount;
    @SerializedName("booking_date")
    private String bookingDate;
    @SerializedName("booking_time")
    private String bookingTime;
    @SerializedName("status")
    private String status;
    @SerializedName("ride_status")
    private String rideStatus;
    @SerializedName("booking_type")
    private String bookingType;
    @SerializedName("rating")
    private String rating;
    @SerializedName("driver_name")
    private String driverName;
    @SerializedName("driver_custom_rate")
    private String driverCustomRate;
    @SerializedName("custom_price")
    private String customPrice;

    public String getOrderId() { return orderId; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getAmount() { return amount; }
    public String getFare() { return fare; }
    public String getTotalFare() { return totalFare; }
    public String getPayableAmount() { return payableAmount; }
    public String getBookingDate() { return bookingDate; }
    public String getBookingTime() { return bookingTime; }
    public String getStatus() { return status; }
    public String getRideStatus() { return rideStatus; }
    public String getBookingType() { return bookingType; }
    public String getRating() { return rating; }
    public String getDriverName() { return driverName; }
    public String getDriverCustomRate() { return driverCustomRate; }
    public String getCustomPrice() { return customPrice; }
}
