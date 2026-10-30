package com.oncab.user.ui.shuttle;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.oncab.user.ApiManager.RetrofitHelper;
import com.oncab.user.R;
import com.oncab.user.Utils.Constants;
import com.oncab.user.Utils.WebService;
import com.oncab.user.databinding.FragmentShuttleBookingsBinding;
import com.oncab.user.ui.shuttle.model.BusBookingData;
import com.oncab.user.ui.shuttle.model.BusUserBookingsRequest;
import com.oncab.user.ui.shuttle.model.BusUserBookingsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Faithful port of ShuttleBookingsFragment.kt — Upcoming / Completed / Cancelled
 * tabs over the BUS user-bookings endpoint.
 */
public class ShuttleBookingsFragment extends Fragment {

    private static final String TAG = "ShuttleBookings";
    private static final String[] TABS = {"Upcoming", "Completed", "Cancelled"};
    private static final String[] STATUS_KEYS = {"upcoming", "completed", "cancelled"};

    private FragmentShuttleBookingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShuttleBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        for (String tab : TABS) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tab));
        }
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadBookings(STATUS_KEYS[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        loadBookings(STATUS_KEYS[0]);
    }

    private void loadBookings(String statusKey) {
        android.content.SharedPreferences sp = requireContext()
                .getSharedPreferences(Constants.FILE_NAME, 0);
        String mobile = sp.getString(Constants.USER_MOBILE, "");
        WebService api = RetrofitHelper.getRetrofitInstance().create(WebService.class);
        api.getBusUserBookings(new BusUserBookingsRequest(mobile, "", statusKey))
                .enqueue(new Callback<BusUserBookingsResponse>() {
                    @Override
                    public void onResponse(Call<BusUserBookingsResponse> call,
                                           Response<BusUserBookingsResponse> response) {
                        List<BusBookingData> list = new ArrayList<>();
                        BusUserBookingsResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.getData() != null
                                && body.getData().getBookings() != null) {
                            list.addAll(body.getData().getBookings());
                        }
                        render(list, statusKey);
                    }

                    @Override
                    public void onFailure(Call<BusUserBookingsResponse> call, Throwable t) {
                        Log.e(TAG, "bus bookings: " + t.getMessage());
                        render(new ArrayList<>(), statusKey);
                    }
                });
    }

    private void render(List<BusBookingData> list, String statusKey) {
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(new BusBookingAdapter(list));
    }

    /** Simple card list row built from the bus booking fields. */
    private static class BusBookingAdapter extends RecyclerView.Adapter<BusBookingAdapter.Holder> {
        private final List<BusBookingData> items;

        BusBookingAdapter(List<BusBookingData> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView title = new TextView(parent.getContext());
            int pad = (int) (16 * parent.getResources().getDisplayMetrics().density);
            title.setPadding(pad, pad, pad, pad);
            title.setTextSize(15);
            return new Holder(title);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            BusBookingData b = items.get(position);
            holder.title.setText(b.getBookingReference() + "  •  "
                    + safe(b.getOriginStop()) + " → " + safe(b.getDestinationStop())
                    + "\n" + safe(b.getTravelDate()) + "   Seats " + safe(b.getSeatNumbers())
                    + "   ₹" + safe(b.getTotalFare()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static String safe(String s) {
            return s == null ? "-" : s;
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView title;

            Holder(TextView v) {
                super(v);
                title = v;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
