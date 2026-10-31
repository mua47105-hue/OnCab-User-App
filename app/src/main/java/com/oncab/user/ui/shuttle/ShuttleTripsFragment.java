package com.oncab.user.ui.shuttle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.oncab.user.databinding.FragmentShuttleTripsBinding;

/**
 * Faithful port of ShuttleTripsFragment.kt — trip search header + empty state,
 * matching the original layout (Upcoming/Completed/Cancelled friendly empty copy).
 */
public class ShuttleTripsFragment extends Fragment {

    private FragmentShuttleTripsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShuttleTripsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.tvEmptyTitle.setText("No trips yet");
        binding.tvEmptyMessage.setText("Search for bus routes to book your next journey.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
