package com.oncab.user.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.oncab.user.Activity.PhoneValidation.PhoneNumberActivity;
import com.oncab.user.R;
import com.oncab.user.databinding.ActivityPermissionBinding;

import java.util.List;

/** Faithful port of PermissionActivity.kt (Dexter location + POST_NOTIFICATIONS). */
public class PermissionActivity extends AppCompatActivity {

    private ActivityPermissionBinding binding;
    private ActivityResultLauncher<String> notificationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notificationLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) {
                        continueToNextScreen();
                    } else {
                        Snackbar.make(binding.getRoot(),
                                "Please grant Notification permission from App Settings",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });

        binding.allowBtn.setOnClickListener(v -> checkNotificationThenLocation());
        requestPermissions();
    }

    private void checkNotificationThenLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            continueToNextScreen();
        } else {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void continueToNextScreen() {
        startActivity(new Intent(this, PhoneNumberActivity.class));
        finish();
    }

    private void requestPermissions() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // user may still press allowBtn after granting
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(this::showSettingsDialog)
                .check();
    }

    private void showSettingsDialog(DexterError error) {
        new AlertDialog.Builder(this)
                .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
                .setPositiveButton("GOTO SETTINGS", (d, w) -> {
                    d.cancel();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivityForResult(intent, 101);
                })
                .setNegativeButton("Cancel", (d, w) -> d.cancel())
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            continueToNextScreen();
        }
    }
}
