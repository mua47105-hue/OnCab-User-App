package com.oncab.user.Activity.OtpValidation.Model;

import com.google.gson.annotations.SerializedName;

public class LoginModel {
    @SerializedName("status")
    private Integer status;
    @SerializedName("message")
    private Object message;
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("referral_code")
    private String referralCode;

    public Integer getStatus() { return status; }
    public Object getMessage() { return message; }
    public Integer getUserId() { return userId; }
    public String getReferralCode() { return referralCode; }
}
