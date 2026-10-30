package com.oncab.user.Utils;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class UserCreateResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private JsonElement message;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("referral_code")
    private String referralCode;
    @SerializedName("photo")
    private String photo;

    public String getStatus() { return status; }
    public JsonElement getMessage() { return message; }
    public String getUserId() { return userId; }
    public String getReferralCode() { return referralCode; }
    public String getPhoto() { return photo; }
}
