package com.oncab.user.Activity.PhoneValidation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.oncab.user.Activity.OtpValidation.OtpVerificationActivity;
import com.oncab.user.ApiManager.RetrofitHelper;
import com.oncab.user.R;
import com.oncab.user.Utils.WebService;
import com.oncab.user.databinding.ActivityPhoneNumberBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Faithful port of PhoneNumberActivity.kt — 10-digit mobile + otp-send. */
public class PhoneNumberActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OTP_FLOW";

    private ActivityPhoneNumberBinding binding;
    private boolean isClickable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.etMobileNumber.requestFocus();
        binding.etMobileNumber.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.etMobileNumber, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200L);

        binding.signInBtn.setOnClickListener(this);
        binding.ivBack.setOnClickListener(this);
        binding.signInBtn.setVisibility(View.GONE);

        binding.etMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    isClickable = true;
                    binding.signInBtn.setVisibility(View.VISIBLE);
                    binding.signInBtn.setAlpha(1.0f);
                } else {
                    isClickable = false;
                    binding.signInBtn.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v != null && v.getId() == R.id.signInBtn) {
            if (isClickable) {
                sendOtp();
            }
        } else if (v != null && v.getId() == R.id.ivBack) {
            onBackPressed();
        }
    }

    private void sendOtp() {
        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait..");
        dialog.setCancelable(false);
        dialog.show();
        long start = System.currentTimeMillis();

        Log.d(TAG, "Requesting OTP for: " + binding.etMobileNumber.getText());
        api.otpSend(binding.etMobileNumber.getText().toString())
                .enqueue(new Callback<OtpModel>() {
                    @Override
                    public void onResponse(Call<OtpModel> call, Response<OtpModel> response) {
                        Log.d(TAG, "Response received in "
                                + (System.currentTimeMillis() - start) + "ms, status: "
                                + response.code());
                        OtpModel body = response.body();
                        if (response.isSuccessful() && body != null) {
                            if (body.getStatus() != null && body.getStatus() == 200) {
                                dialog.dismiss();
                                Intent intent = new Intent(PhoneNumberActivity.this,
                                        OtpVerificationActivity.class);
                                intent.putExtra("phone", binding.etMobileNumber.getText().toString());
                                intent.putExtra("otp", String.valueOf(body.getOtp()));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                dialog.dismiss();
                                String message = body.getMessage() != null
                                        ? body.getMessage() : "Error";
                                Toast.makeText(PhoneNumberActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            dialog.dismiss();
                            Toast.makeText(PhoneNumberActivity.this,
                                    "Response failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<OtpModel> call, Throwable t) {
                        dialog.dismiss();
                        Toast.makeText(PhoneNumberActivity.this,
                                "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
