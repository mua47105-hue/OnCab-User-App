package com.oncab.user.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.oncab.user.R;
import com.oncab.user.databinding.FragmentConfirmLocationBinding;

/** Original layout with back handling; deeper booking flow continues in-app. */
public class ConfirmLocationFragment extends Fragment {

    private FragmentConfirmLocationBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.ivBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_new_home_map));
        binding.cvBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_new_home_map));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
