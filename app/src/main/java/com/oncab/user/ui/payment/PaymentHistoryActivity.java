package com.oncab.user.ui.payment;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.oncab.user.R;
import com.oncab.user.databinding.ActivityPaymenthistoryBinding;

/** Original screen from the rebuilt OnCab app (activity_paymenthistory). */
public class PaymentHistoryActivity extends AppCompatActivity {

    private ActivityPaymenthistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymenthistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backBtn.setOnClickListener(v -> finish());
    }
}
