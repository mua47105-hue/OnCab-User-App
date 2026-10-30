package com.oncab.user.ui.account;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityUpdateProfileBinding;

/** Original screen from the rebuilt OnCab app (activity_update_profile). */
public class UpdateProfile extends AppCompatActivity {

    private ActivityUpdateProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backBtn.setOnClickListener(v -> finish());
    }
}
