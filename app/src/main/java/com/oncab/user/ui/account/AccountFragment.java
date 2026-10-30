package com.oncab.user.ui.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.oncab.user.Activity.PhoneValidation.PhoneNumberActivity;
import com.oncab.user.ApiManager.RetrofitHelper;
import com.oncab.user.Model.ProfileModel;
import com.oncab.user.R;
import com.oncab.user.Utils.Constants;
import com.oncab.user.Utils.WebService;
import com.oncab.user.databinding.FragmentAccountBinding;
import com.oncab.user.ui.help.AboutActivity;
import com.oncab.user.ui.help.SupportActivity;
import com.oncab.user.ui.payment.PaymentHistoryActivity;
import com.oncab.user.ui.referral.ReferralActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Faithful port of AccountFragment.kt — profile, cards, logout. */
public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sp = requireContext().getSharedPreferences(Constants.FILE_NAME, 0);
        binding.userName.setText(sp.getString(Constants.USER_NAME, "OnCab User"));
        binding.phoneNumber.setText("+91" + sp.getString(Constants.USER_MOBILE, ""));
        binding.email.setText(sp.getString(Constants.USER_EMAIL, ""));

        String photo = sp.getString("user_photo", "");
        if (photo.length() > 0 && !"null".equalsIgnoreCase(photo)) {
            Glide.with(this).load(photo).centerCrop()
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .into(binding.profilePic);
        } else {
            binding.profilePic.setImageResource(R.drawable.avatar_1);
        }

        binding.backBtn.setOnClickListener(v ->
                requireActivity().onBackPressedDispatcher.onBackPressed());
        binding.cardSupport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SupportActivity.class)));
        binding.cardPayment.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PaymentHistoryActivity.class)));
        binding.cardRefer.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ReferralActivity.class)));
        binding.cardAbout.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AboutActivity.class)));
        binding.cardSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));
        binding.cardYourRides.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PaymentHistoryActivity.class)));
        binding.btnLogout.setOnClickListener(v -> logout());
        binding.makeMoreMoneyCard.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ReferralActivity.class)));

        loadProfile();
    }

    private void loadProfile() {
        String userId = requireContext().getSharedPreferences(Constants.FILE_NAME, 0)
                .getString(Constants.USER_ID, "");
        if (userId.length() == 0) {
            return;
        }
        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        api.getProfile(userId).enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                ProfileModel body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    SharedPreferences.Editor editor = requireContext()
                            .getSharedPreferences(Constants.FILE_NAME, 0).edit();
                    if (body.getData().getName() != null) {
                        binding.userName.setText(body.getData().getName());
                        editor.putString(Constants.USER_NAME, body.getData().getName());
                    }
                    if (body.getData().getEmail() != null) {
                        binding.email.setText(body.getData().getEmail());
                        editor.putString(Constants.USER_EMAIL, body.getData().getEmail());
                    }
                    if (body.getData().getMobile() != null) {
                        binding.phoneNumber.setText("+91" + body.getData().getMobile());
                        editor.putString(Constants.USER_MOBILE, body.getData().getMobile());
                    }
                    if (body.getData().getWallet() != null) {
                        editor.putString(Constants.USER_WALLET, body.getData().getWallet());
                    }
                    if (body.getData().getPhoto() != null
                            && body.getData().getPhoto().length() > 0
                            && !"null".equalsIgnoreCase(body.getData().getPhoto())) {
                        editor.putString("user_photo", body.getData().getPhoto());
                        Glide.with(AccountFragment.this).load(body.getData().getPhoto())
                                .placeholder(R.drawable.avatar_1)
                                .error(R.drawable.avatar_1)
                                .into(binding.profilePic);
                    }
                    editor.apply();
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                // profile stays from prefs
            }
        });
    }

    private void logout() {
        String userId = requireContext().getSharedPreferences(Constants.FILE_NAME, 0)
                .getString(Constants.USER_ID, "");
        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        api.logout(userId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call,
                                   Response<okhttp3.ResponseBody> response) {
                clearAndExit();
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                clearAndExit();
            }
        });
    }

    private void clearAndExit() {
        requireContext().getSharedPreferences(Constants.FILE_NAME, 0)
                .edit().remove(Constants.USER_ID).apply();
        Intent intent = new Intent(requireContext(), PhoneNumberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
