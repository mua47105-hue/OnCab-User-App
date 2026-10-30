package com.oncab.user.ui.unused;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityFaltuBinding;

/** Original screen from the rebuilt OnCab app (activity_faltu). */
public class CustomActivity extends AppCompatActivity {

    private ActivityFaltuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaltuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
