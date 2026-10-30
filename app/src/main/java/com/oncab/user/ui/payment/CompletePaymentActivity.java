package com.oncab.user.ui.payment;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityCompletePaymentBinding;

/** Original screen from the rebuilt OnCab app (activity_complete_payment). */
public class CompletePaymentActivity extends AppCompatActivity {

    private ActivityCompletePaymentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompletePaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
