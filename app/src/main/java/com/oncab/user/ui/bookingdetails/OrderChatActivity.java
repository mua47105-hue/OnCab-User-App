package com.oncab.user.ui.bookingdetails;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityOrderChatBinding;

/** Original screen from the rebuilt OnCab app (activity_order_chat). */
public class OrderChatActivity extends AppCompatActivity {

    private ActivityOrderChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
