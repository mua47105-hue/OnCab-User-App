package com.oncab.user.Utils;

import com.oncab.user.Activity.PhoneValidation.OtpModel;
import com.oncab.user.Model.BannerResponseModel;
import com.oncab.user.Model.ProfileModel;
import com.oncab.user.ui.bookinghistory.model.OrderListModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/** Core REST surface of the original WebService.kt (Java Call-based port). */
public interface WebService {

    @FormUrlEncoded
    @POST("otp-send")
    Call<OtpModel> otpSend(@Field("mobile") String mobile);

    @FormUrlEncoded
    @POST("user-login")
    Call<com.oncab.user.Activity.OtpValidation.Model.LoginModel> login(
            @Field("mobile") String mobile, @Field("device_id") String deviceId);

    @Multipart
    @POST("user-create")
    Call<UserCreateResponse> userCreate(
            @Part("user_id") RequestBody userId,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("referral") RequestBody referral,
            @Part(Constants.REFERRAL_CODE) RequestBody referralCode,
            @Part("refer_code") RequestBody referCode,
            @Part MultipartBody.Part photo);

    @FormUrlEncoded
    @POST("user-profile")
    Call<ProfileModel> getProfile(@Field("user_id") String userId);

    @Multipart
    @POST("user-profile-update")
    Call<ProfileModel> updateProfile(
            @Part("user_id") RequestBody userId,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part MultipartBody.Part photo);

    @FormUrlEncoded
    @POST("user-order-list")
    Call<OrderListModel> getOrderList(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("get-banners")
    Call<BannerResponseModel> getBanners(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("booking-status")
    Call<com.oncab.user.Model.BookingStatusModel> bookingStatus(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("logout")
    Call<ResponseBody> logout(@Field("user_id") String userId);

    @POST(Urls.BUS_BASE_URL + "user-bookings")
    Call<com.oncab.user.ui.shuttle.model.BusUserBookingsResponse> getBusUserBookings(
            @Body com.oncab.user.ui.shuttle.model.BusUserBookingsRequest request);

    @GET(Urls.BUS_BASE_URL + "user-refunds")
    Call<ResponseBody> getBusUserRefunds(@Query("passenger_mobile") String passengerMobile);
}
