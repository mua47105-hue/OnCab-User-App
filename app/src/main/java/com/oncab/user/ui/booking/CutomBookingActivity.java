package com.oncab.user.ui.booking;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.CustomBookingLayoutBinding;

/** Original screen from the rebuilt OnCab app (custom_booking_layout). */
public class CutomBookingActivity extends AppCompatActivity {

    private CustomBookingLayoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CustomBookingLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
