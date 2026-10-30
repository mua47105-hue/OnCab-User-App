package com.oncab.user.Activity.OtpValidation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;
import com.oncab.user.Activity.PhoneValidation.UserDetails;
import com.oncab.user.ApiManager.RetrofitHelper;
import com.oncab.user.MainActivity;
import com.oncab.user.R;
import com.oncab.user.Utils.Constants;
import com.oncab.user.Utils.WebService;
import com.oncab.user.databinding.ActivityOtpVerificationBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Faithful port of OtpVerificationActivity.kt:
 * 6-digit PinView, 5-minute expiry, 30s resend timer, FCM token retry,
 * user-login -> UserDetails (new) / MainActivity (existing).
 */
public class OtpVerificationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OTP_FLOW";

    private ActivityOtpVerificationBinding binding;
    private String token = "";
    private String generatedOtp = "";
    private long otpGeneratedTime;
    private boolean canResend = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.otpView.setAnimationEnable(true);
        fetchFcmToken();
        generatedOtp = String.valueOf(getIntent().getStringExtra("otp"));
        otpGeneratedTime = System.currentTimeMillis();

        binding.mobileNumber.setText("+91" + getIntent().getStringExtra("phone"));
        binding.tvResendOTP.setOnClickListener(this);
        Log.d("CURRENT_OTP", generatedOtp);

        binding.otpView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() != 6) {
                    return;
                }
                binding.otpView.setEnabled(false);
                validateOtp();
            }
        });

        startResendTimer();
    }

    private void fetchFcmToken() {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    token = task.getResult();
                    Log.d("FCM_TOKEN", token);
                }
            });
        } catch (Exception e) {
            // Firebase unavailable (no valid backend config): login proceeds without token
        }
    }

    private void validateOtp() {
        String entered = String.valueOf(binding.otpView.getText()).trim();
        if (entered.length() == 0) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (entered.length() != 6) {
            Toast.makeText(this, "Please enter valid OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (System.currentTimeMillis() - otpGeneratedTime > 300000) {
            resetOtpView();
            Toast.makeText(this, "OTP Expired. Please resend OTP", Toast.LENGTH_LONG).show();
            return;
        }
        if (entered.equals(generatedOtp)) {
            loginApi();
            return;
        }
        resetOtpView();
        Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show();
    }

    private void resetOtpView() {
        binding.otpView.setText("");
        binding.otpView.setEnabled(true);
        binding.otpView.requestFocus();
    }

    @Override
    public void onClick(View v) {
        if (v != null && v.getId() == R.id.tv_ResendOTP) {
            if (canResend) {
                resendOtp();
            } else {
                Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resendOtp() {
        generatedOtp = String.valueOf(getIntent().getStringExtra("otp"));
        otpGeneratedTime = System.currentTimeMillis();
        startResendTimer();
        Toast.makeText(this, "OTP Resent Successfully", Toast.LENGTH_SHORT).show();
    }

    private void startResendTimer() {
        canResend = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvResendOTP.setText("Resend OTP in " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                canResend = true;
                binding.tvResendOTP.setText("Resend OTP");
                Toast.makeText(OtpVerificationActivity.this,
                        "Now you can resend OTP", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void loginApi() {
        SharedPreferences sp = getSharedPreferences(Constants.FILE_NAME, 0);
        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();

        if (token.length() == 0) {
            fetchTokenWithRetry(3, api, sp, dialog);
        } else {
            proceedWithLogin(api, sp, dialog);
        }
    }

    private void fetchTokenWithRetry(int retriesLeft, WebService api,
                                     SharedPreferences sp, ProgressDialog dialog) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    token = task.getResult();
                    sp.edit().putString(Constants.DEVICE_ID, token).apply();
                    proceedWithLogin(api, sp, dialog);
                } else if (retriesLeft > 0) {
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> fetchTokenWithRetry(retriesLeft - 1, api, sp, dialog), 2000L);
                } else {
                    if (token.length() == 0) {
                        token = "token_error";
                    }
                    proceedWithLogin(api, sp, dialog);
                }
            });
        } catch (Exception e) {
            if (token.length() == 0) {
                token = "token_error";
            }
            proceedWithLogin(api, sp, dialog);
        }
    }

    private void proceedWithLogin(WebService api, SharedPreferences sp, ProgressDialog dialog) {
        final String phone = getIntent().getStringExtra("phone");
        api.login(phone, token).enqueue(new Callback<com.oncab.user.Activity.OtpValidation.Model.LoginModel>() {
            @Override
            public void onResponse(Call<com.oncab.user.Activity.OtpValidation.Model.LoginModel> call,
                                   Response<com.oncab.user.Activity.OtpValidation.Model.LoginModel> response) {
                com.oncab.user.Activity.OtpValidation.Model.LoginModel body = response.body();
                if (response.isSuccessful() && body != null && body.getStatus() != null
                        && (body.getStatus() == 200 || body.getStatus() == 201)) {
                    String userId = body.getUserId() != null
                            ? String.valueOf(body.getUserId()) : "";
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(Constants.USER_ID, userId);
                    editor.putString(Constants.USER_MOBILE, phone);
                    if (body.getReferralCode() != null) {
                        editor.putString(Constants.REFERRAL_CODE, body.getReferralCode());
                    }
                    editor.apply();
                    dialog.dismiss();

                    Intent target;
                    if (body.getStatus() == 201) {
                        // New user: complete profile first
                        target = new Intent(OtpVerificationActivity.this, UserDetails.class);
                    } else {
                        target = new Intent(OtpVerificationActivity.this, MainActivity.class);
                    }
                    target.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(target);
                    finish();
                } else {
                    dialog.dismiss();
                    String msg = "Login failed";
                    if (body != null && body.getMessage() != null) {
                        msg = String.valueOf(body.getMessage());
                    }
                    Toast.makeText(OtpVerificationActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.oncab.user.Activity.OtpValidation.Model.LoginModel> call,
                                  Throwable t) {
                dialog.dismiss();
                Toast.makeText(OtpVerificationActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding = null;
    }
}
