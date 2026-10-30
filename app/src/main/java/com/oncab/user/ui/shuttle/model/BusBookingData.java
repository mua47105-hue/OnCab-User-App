package com.oncab.user.ui.shuttle.model;

import com.google.gson.annotations.SerializedName;

public class BusBookingData {
    @SerializedName("id")
    private String id;
    @SerializedName("booking_id")
    private String bookingId;
    @SerializedName("booking_reference")
    private String bookingReference;
    @SerializedName("booking_status")
    private String bookingStatus;
    @SerializedName("status")
    private String status;
    @SerializedName("origin_stop")
    private String originStop;
    @SerializedName("destination_stop")
    private String destinationStop;
    @SerializedName("travel_date")
    private String travelDate;
    @SerializedName("boarding_time")
    private String boardingTime;
    @SerializedName("boarding_pin")
    private String boardingPin;
    @SerializedName("boarding_pass_code")
    private String boardingPassCode;
    @SerializedName("seat_numbers")
    private String seatNumbers;
    @SerializedName("total_seats")
    private String totalSeats;
    @SerializedName("total_fare")
    private String totalFare;
    @SerializedName("final_amount")
    private String finalAmount;
    @SerializedName("discount_amount")
    private String discountAmount;
    @SerializedName("payment_status")
    private String paymentStatus;
    @SerializedName("passenger_name")
    private String passengerName;
    @SerializedName("passenger_mobile")
    private String passengerMobile;
    @SerializedName("passenger_email")
    private String passengerEmail;
    @SerializedName("qr_token")
    private String qrToken;
    @SerializedName("cancellation_reason")
    private String cancellationReason;
    @SerializedName("cancelled_at")
    private String cancelledAt;

    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public String getBookingReference() { return bookingReference; }
    public String getBookingStatus() { return bookingStatus; }
    public String getStatus() { return status; }
    public String getOriginStop() { return originStop; }
    public String getDestinationStop() { return destinationStop; }
    public String getTravelDate() { return travelDate; }
    public String getBoardingTime() { return boardingTime; }
    public String getBoardingPin() { return boardingPin; }
    public String getBoardingPassCode() { return boardingPassCode; }
    public String getSeatNumbers() { return seatNumbers; }
    public String getTotalSeats() { return totalSeats; }
    public String getTotalFare() { return totalFare; }
    public String getFinalAmount() { return finalAmount; }
    public String getDiscountAmount() { return discountAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerMobile() { return passengerMobile; }
    public String getPassengerEmail() { return passengerEmail; }
    public String getQrToken() { return qrToken; }
    public String getCancellationReason() { return cancellationReason; }
    public String getCancelledAt() { return cancelledAt; }
}
