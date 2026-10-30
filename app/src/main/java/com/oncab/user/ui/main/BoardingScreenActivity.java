package com.oncab.user.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.oncab.user.Activity.PhoneValidation.PhoneNumberActivity;
import com.oncab.user.MainActivity;
import com.oncab.user.R;
import com.oncab.user.Utils.Constants;
import com.oncab.user.databinding.ActivityBoardingScreenBinding;

/** Faithful port of BoardingScreenActivity.kt */
public class BoardingScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityBoardingScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBoardingScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Privacy-policy link is the second child of llBottomContent (as in original)
        View privacyView = binding.llBottomContent.getChildAt(1);
        if (privacyView != null) {
            privacyView.setOnClickListener(v -> startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("http://oncab.in/privacy-policy.php"))));
        }

        binding.txtContinue.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences sp = getSharedPreferences(Constants.FILE_NAME, 0);
        if (v != null && v.getId() == R.id.txtContinue) {
            if (sp.contains(Constants.USER_ID)
                    && sp.getString(Constants.USER_ID, "").length() > 0) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, PhoneNumberActivity.class));
                finish();
            }
        }
    }
}
