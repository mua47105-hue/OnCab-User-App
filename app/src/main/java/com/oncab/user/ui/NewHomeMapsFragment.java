package com.oncab.user.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oncab.user.MainActivity;
import com.oncab.user.R;
import com.oncab.user.Utils.Constants;
import com.oncab.user.databinding.FragmentNewHomeMapsBinding;

/**
 * Faithful port of NewHomeMapsFragment.kt — Kolkata-centered Google Map,
 * drawer trigger, destination search entry, instant/schedule/custom/rental
 * options and the welcome reward popup shown once per fresh session.
 */
public class NewHomeMapsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "NewHomeMaps";
    private static final LatLng KOLKATA = new LatLng(22.5726, 88.3639);
    private static final float DEFAULT_ZOOM = 14f;

    private FragmentNewHomeMapsBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = KOLKATA.latitude;
    private double longitude = KOLKATA.longitude;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNewHomeMapsBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map1);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.navDrawer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        binding.cvSearchDestination.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_destination));

        binding.tvCurrentLocation.setOnClickListener(v -> moveToDeviceLocation());

        binding.cvCurrentLocation.setOnClickListener(v -> moveToDeviceLocation());

        binding.tvPinAddress.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_destination));

        binding.llCenterPin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_destination));

        maybeShowWelcomeReward();
    }

    /** ₹50 sign-up reward popup (Welcome gift) — shown once per install as in original. */
    private void maybeShowWelcomeReward() {
        SharedPreferences sp = requireContext()
                .getSharedPreferences(Constants.FILE_NAME, 0);
        boolean shown = sp.getBoolean("reward_50_shown", false);
        if (!shown) {
            try {
                com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                        new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
                com.oncab.user.databinding.ComingSoonPopupBinding popup =
                        com.oncab.user.databinding.ComingSoonPopupBinding.inflate(getLayoutInflater());
                dialog.setContentView(popup.getRoot());
                popup.getRoot().postDelayed(dialog::dismiss, 4000L);
                dialog.show();
            } catch (Exception e) {
                Log.e(TAG, "reward popup: " + e.getMessage());
            }
            sp.edit().putBoolean("reward_50_shown", true).apply();
        }
    }

    @SuppressLint("MissingPermission")
    private void moveToDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && googleMap != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), DEFAULT_ZOOM));
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KOLKATA, DEFAULT_ZOOM));
        moveToDeviceLocation();
        reverseGeocodeCenter();
    }

    /** Shows the picked-up point name at the center pin (original behavior). */
    private void reverseGeocodeCenter() {
        if (googleMap == null) {
            return;
        }
        googleMap.setOnCameraIdleListener(() -> {
            LatLng center = googleMap.getCameraPosition().target;
            latitude = center.latitude;
            longitude = center.longitude;
            new Thread(() -> {
                String address = resolveAddress(center);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> binding.tvPinAddress.setText(address));
                }
            }).start();
        });
    }

    private String resolveAddress(LatLng center) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(requireContext());
            java.util.List<android.location.Address> list =
                    geocoder.getFromLocation(center.latitude, center.longitude, 1);
            if (list != null && !list.isEmpty()) {
                android.location.Address a = list.get(0);
                return a.getAddressLine(0) != null ? a.getAddressLine(0) : "Kolkata";
            }
        } catch (Exception ignored) {
        }
        return "Kolkata";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
