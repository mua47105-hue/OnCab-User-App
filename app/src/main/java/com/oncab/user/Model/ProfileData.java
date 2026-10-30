package com.oncab.user.Model;

import com.google.gson.annotations.SerializedName;

public class ProfileData {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("mobile")
    private String mobile;
    @SerializedName("photo")
    private String photo;
    @SerializedName("wallet")
    private String wallet;
    @SerializedName("referral_code")
    private String referralCode;
    @SerializedName("referral")
    private String referral;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getPhoto() { return photo; }
    public String getWallet() { return wallet; }
    public String getReferralCode() { return referralCode; }
    public String getReferral() { return referral; }
}
