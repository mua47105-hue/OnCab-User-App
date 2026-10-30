package com.oncab.user.ui.booking;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityBookingCompleteBinding;

/** Original screen from the rebuilt OnCab app (activity_booking_complete). */
public class BookingComplete extends AppCompatActivity {

    private ActivityBookingCompleteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingCompleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
