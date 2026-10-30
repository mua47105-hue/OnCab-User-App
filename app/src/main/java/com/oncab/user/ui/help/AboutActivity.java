package com.oncab.user.ui.help;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.FragmentAboutBinding;

/** Original screen from the rebuilt OnCab app (fragment_about). */
public class AboutActivity extends AppCompatActivity {

    private FragmentAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backBtn.setOnClickListener(v -> finish());
    }
}
