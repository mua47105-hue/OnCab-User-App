package com.oncab.user.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.oncab.user.ApiManager.RetrofitHelper;
import com.oncab.user.MainActivity;
import com.oncab.user.Model.BannerData;
import com.oncab.user.Model.BannerResponseModel;
import com.oncab.user.Utils.Constants;
import com.oncab.user.Utils.Urls;
import com.oncab.user.Utils.WebService;
import com.oncab.user.databinding.ActivitySplashScreenBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Faithful port of SplashScreenActivity.kt:
 * splash layout + fetch promo banner (get-banners) + slide-out navigation.
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static final long BANNER_DISPLAY_MS = 5000L;
    private static final long FETCH_TIMEOUT_MS = 15000L;

    private ActivitySplashScreenBinding binding;
    private boolean isNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        fetchBannerAndNavigate();
    }

    private Intent targetIntent() {
        SharedPreferences sp = getSharedPreferences(Constants.FILE_NAME, 0);
        if (sp.contains(Constants.USER_ID) && sp.getString(Constants.USER_ID, "").length() > 0) {
            return new Intent(this, MainActivity.class);
        }
        return new Intent(this, BoardingScreenActivity.class);
    }

    private void fetchBannerAndNavigate() {
        binding.progressBar.setVisibility(View.VISIBLE);
        final Intent target = targetIntent();

        // Hard timeout: never block navigation more than 15s
        Handler timeout = new Handler(Looper.getMainLooper());
        timeout.postDelayed(() -> startLogoExitAndFinish(target), FETCH_TIMEOUT_MS);

        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        api.getBanners(getSharedPreferences(Constants.FILE_NAME, 0)
                .getString(Constants.USER_ID, "")).enqueue(new Callback<BannerResponseModel>() {
            @Override
            public void onResponse(Call<BannerResponseModel> call, Response<BannerResponseModel> response) {
                BannerResponseModel body = response.body();
                String img = null;
                if (body != null && body.getData() != null) {
                    List<BannerData> list = body.getData();
                    if (!list.isEmpty() && list.get(0).getImg() != null) {
                        img = list.get(0).getImg();
                        if (img.startsWith("/")) {
                            img = Urls.IMAGE_URL + img.substring(1);
                        } else if (!img.startsWith("http")) {
                            img = Urls.IMAGE_URL + img;
                        }
                    }
                }
                final String url = img;
                if (url != null) {
                    Glide.with(SplashScreenActivity.this)
                            .load(url)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e,
                                                            Object model,
                                                            com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                            boolean isFirstResource) {
                                    showBannerThenNavigate(null, targetIntent, timeout);
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                               Object model,
                                                               com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                               com.bumptech.glide.load.DataSource dataSource,
                                                               boolean isFirstResource) {
                                    showBannerThenNavigate(url, targetIntent, timeout);
                                    return true;
                                }
                            })
                            .into(binding.ivSplashBanner);
                } else {
                    showBannerThenNavigate(null, targetIntent, timeout);
                }
            }

            @Override
            public void onFailure(Call<BannerResponseModel> call, Throwable t) {
                showBannerThenNavigate(null, target, timeout);
            }
        });
    }

    private void showBannerThenNavigate(String url, Intent target, Handler timeout) {
        timeout.removeCallbacksAndMessages(null);
        if (isNavigated) {
            return;
        }
        binding.progressBar.setVisibility(View.GONE);
        if (url != null) {
            binding.ivSplashBanner.bringToFront();
            binding.ivSplashBanner.setVisibility(View.VISIBLE);
        }
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> startLogoExitAndFinish(target), BANNER_DISPLAY_MS);
    }

    private void startLogoExitAndFinish(Intent target) {
        if (isNavigated) {
            return;
        }
        isNavigated = true;
        binding.getRoot().animate()
                .translationX(getResources().getDisplayMetrics().widthPixels)
                .setDuration(700L)
                .withEndAction(() -> {
                    startActivity(target);
                    overridePendingTransition(0, 0);
                    finish();
                })
                .start();
    }
}
