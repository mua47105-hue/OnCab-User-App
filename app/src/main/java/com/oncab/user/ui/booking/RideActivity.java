package com.oncab.user.ui.booking;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityRideBinding;

/** Original screen from the rebuilt OnCab app (activity_ride). */
public class RideActivity extends AppCompatActivity {

    private ActivityRideBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
