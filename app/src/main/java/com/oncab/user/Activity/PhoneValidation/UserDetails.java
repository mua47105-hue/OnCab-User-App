package com.oncab.user.Activity.PhoneValidation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.oncab.user.ApiManager.RetrofitHelper;
import com.oncab.user.MainActivity;
import com.oncab.user.R;
import com.oncab.user.Utils.Constants;
import com.oncab.user.Utils.UserCreateResponse;
import com.oncab.user.Utils.WebService;
import com.oncab.user.databinding.ActivityUserDetailsBinding;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Faithful port of UserDetails.kt — profile completion (user-create) then MainActivity.
 */
public class UserDetails extends AppCompatActivity implements View.OnClickListener {

    private ActivityUserDetailsBinding binding;
    private SharedPreferences sp;
    private String selectedPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sp = getSharedPreferences(Constants.FILE_NAME, 0);

        binding.submit.setOnClickListener(this);
        binding.tvTerms.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://oncab.in/privacy-policy.php"))));
        binding.profileImageContainer.setOnClickListener(v -> pickImage());
    }

    private void pickImage() {
        android.content.Intent intent = new android.content.Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), 77);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 77 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                String path = copyToCache(data);
                if (path != null) {
                    selectedPhotoPath = path;
                    binding.profileImageContainer.setImageURI(data.getData());
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String copyToCache(Intent data) {
        try {
            java.io.InputStream in = getContentResolver().openInputStream(data.getData());
            File out = new File(getCacheDir(), "profile_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fos.close();
            in.close();
            return out.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v != null && v.getId() == R.id.submit) {
            submitProfile();
        }
    }

    private void submitProfile() {
        String name = binding.userName.getText() != null
                ? binding.userName.getText().toString().trim() : "";
        String email = binding.userEmail.getText() != null
                ? binding.userEmail.getText().toString().trim() : "";
        String referralCode = binding.referralCodeEditText.getText() != null
                ? binding.referralCodeEditText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            binding.userName.setError("Please enter your name");
            binding.userName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.userEmail.setError("Please enter a valid email");
            binding.userEmail.requestFocus();
            return;
        }

        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();

        RequestBody userId = body(sp.getString(Constants.USER_ID, ""));
        RequestBody namePart = body(name);
        RequestBody emailPart = body(email);
        RequestBody referralPart = body(referralCode);

        MultipartBody.Part photoPart = null;
        if (!selectedPhotoPath.isEmpty()) {
            File f = new File(selectedPhotoPath);
            if (f.exists()) {
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), f);
                photoPart = MultipartBody.Part.createFormData("photo", f.getName(), fileBody);
            }
        }

        api.userCreate(userId, namePart, emailPart, referralPart, referralPart, referralPart,
                photoPart).enqueue(new Callback<UserCreateResponse>() {
            @Override
            public void onResponse(Call<UserCreateResponse> call, Response<UserCreateResponse> response) {
                UserCreateResponse res = response.body();
                dialog.dismiss();
                if (response.isSuccessful() && res != null && "200".equals(res.getStatus())) {
                    sp.edit()
                            .putString(Constants.USER_NAME, name)
                            .putString(Constants.USER_EMAIL, email)
                            .apply();
                    if (res.getReferralCode() != null) {
                        sp.edit().putString(Constants.REFERRAL_CODE, res.getReferralCode()).apply();
                    }
                    goHome();
                } else {
                    String msg = "Profile update failed";
                    if (res != null && res.getMessage() != null) {
                        if (res.getMessage().isJsonPrimitive()) {
                            msg = res.getMessage().getAsString();
                        } else if (res.getMessage().isJsonArray()) {
                            msg = res.getMessage().toString();
                        }
                    }
                    Toast.makeText(UserDetails.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserCreateResponse> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(UserDetails.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static RequestBody body(String s) {
        return RequestBody.create(MediaType.parse("text/plain"), s == null ? "" : s);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
