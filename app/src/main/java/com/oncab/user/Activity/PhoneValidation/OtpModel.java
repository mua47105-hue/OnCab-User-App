package com.oncab.user.Activity.PhoneValidation;

import com.google.gson.annotations.SerializedName;

public class OtpModel {
    @SerializedName("status")
    private Integer status;
    @SerializedName("otp")
    private Integer otp;
    @SerializedName("message")
    private String message;

    public Integer getStatus() { return status; }
    public Integer getOtp() { return otp; }
    public String getMessage() { return message; }
}
