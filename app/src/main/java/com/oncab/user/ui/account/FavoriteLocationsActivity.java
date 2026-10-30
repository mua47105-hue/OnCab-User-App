package com.oncab.user.ui.account;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityFavoriteLocationsBinding;

/** Original screen from the rebuilt OnCab app (activity_favorite_locations). */
public class FavoriteLocationsActivity extends AppCompatActivity {

    private ActivityFavoriteLocationsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteLocationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backBtn.setOnClickListener(v -> finish());
    }
}
