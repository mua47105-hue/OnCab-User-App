package com.oncab.user.ui.account;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityAddFavoriteMapBinding;

/** Original screen from the rebuilt OnCab app (activity_add_favorite_map). */
public class AddFavoriteMapActivity extends AppCompatActivity {

    private ActivityAddFavoriteMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFavoriteMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backBtn.setOnClickListener(v -> finish());
    }
}
