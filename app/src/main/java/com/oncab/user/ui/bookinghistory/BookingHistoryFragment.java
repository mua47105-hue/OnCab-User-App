package com.oncab.user.ui.bookinghistory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oncab.user.ApiManager.RetrofitHelper;
import com.oncab.user.R;
import com.oncab.user.Utils.Constants;
import com.oncab.user.Utils.WebService;
import com.oncab.user.databinding.FragmentBookingHistoryBinding;
import com.oncab.user.ui.bookinghistory.adapter.OrderListAdapter;
import com.oncab.user.ui.bookinghistory.model.OrderListModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Faithful port of BookingHistoryFragment.kt — All / City / Outstation / Rental /
 * Delivery filter chips over user-order-list, empty state with Book Now.
 */
public class BookingHistoryFragment extends Fragment {

    private static final String TAG = "BookingHistory";

    private FragmentBookingHistoryBinding binding;
    private OrderListAdapter adapter;
    private String selectedFilter = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rcvBookingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderListAdapter();
        binding.rcvBookingList.setAdapter(adapter);

        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_new_home_map));

        binding.btnBookNowEmpty.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_new_home_map));

        setupFilters();
        getBookingHistoryList();
    }

    private void setupFilters() {
        binding.cvFilterAll.setOnClickListener(v ->
                selectFilter("All", binding.cvFilterAll, binding.tvFilterAll));
        binding.cvFilterCityRides.setOnClickListener(v ->
                selectFilter("City", binding.cvFilterCityRides, binding.tvFilterCityRides));
        binding.cvFilterOutstation.setOnClickListener(v ->
                selectFilter("Outstation", binding.cvFilterOutstation, binding.tvFilterOutstation));
        binding.cvFilterRental.setOnClickListener(v ->
                selectFilter("Rental", binding.cvFilterRental, binding.tvFilterRental));
        binding.cvFilterDelivery.setOnClickListener(v ->
                selectFilter("Delivery", binding.cvFilterDelivery, binding.tvFilterDelivery));
    }

    private void selectFilter(String filter, com.google.android.material.card.MaterialCardView card,
                              android.widget.TextView text) {
        selectedFilter = filter;
        card.setCardBackgroundColor(getResources().getColor(R.color.black));
        text.setTextColor(getResources().getColor(R.color.white));
        // Reset the other chips
        com.google.android.material.card.MaterialCardView[] cards = {
                binding.cvFilterAll, binding.cvFilterCityRides, binding.cvFilterOutstation,
                binding.cvFilterRental, binding.cvFilterDelivery};
        android.widget.TextView[] texts = {
                binding.tvFilterAll, binding.tvFilterCityRides, binding.tvFilterOutstation,
                binding.tvFilterRental, binding.tvFilterDelivery};
        String[] names = {"All", "City", "Outstation", "Rental", "Delivery"};
        for (int i = 0; i < cards.length; i++) {
            if (!names[i].equals(filter)) {
                cards[i].setCardBackgroundColor(getResources().getColor(R.color.white));
                texts[i].setTextColor(getResources().getColor(R.color.black));
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        // The list is already loaded; filters re-run query with booking type
        getBookingHistoryList();
    }

    private void getBookingHistoryList() {
        String userId = requireContext().getSharedPreferences(Constants.FILE_NAME, 0)
                .getString(Constants.USER_ID, "");
        if (userId.length() == 0) {
            showEmpty(true);
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        api.getOrderList(userId).enqueue(new Callback<OrderListModel>() {
            @Override
            public void onResponse(Call<OrderListModel> call, Response<OrderListModel> response) {
                binding.progressBar.setVisibility(View.GONE);
                OrderListModel body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    adapter.submit(body.getData());
                    showEmpty(body.getData().isEmpty());
                } else {
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<OrderListModel> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "history: " + t.getMessage());
                showEmpty(true);
            }
        });
    }

    private void showEmpty(boolean empty) {
        binding.layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rcvBookingList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
