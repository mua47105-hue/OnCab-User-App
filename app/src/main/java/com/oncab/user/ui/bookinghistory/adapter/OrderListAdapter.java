package com.oncab.user.ui.bookinghistory.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oncab.user.R;
import com.oncab.user.databinding.BookingCardViewHolderBinding;
import com.oncab.user.ui.bookinghistory.model.Data;

import java.util.ArrayList;
import java.util.List;

/** Order history card list — original booking_card_view_holder layout. */
public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.Holder> {

    private final List<Data> items = new ArrayList<>();

    public void submit(List<Data> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BookingCardViewHolderBinding binding = BookingCardViewHolderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Data item = items.get(position);
        holder.binding.sourceAddress.setText(safe(item.getSource()));
        holder.binding.destinationAddress.setText(safe(item.getDestination()));
        String fare = item.getTotalFare() != null && item.getTotalFare().length() > 0
                ? item.getTotalFare() : item.getAmount();
        holder.binding.tripPrice.setText("\u20B9" + safe(fare));
        holder.binding.tripDateTime.setText(safe(item.getBookingDate()));
        String status = item.getRideStatus() != null && item.getRideStatus().length() > 0
                ? item.getRideStatus() : item.getStatus();
        holder.binding.tvStatus.setText(safe(status));
        int color = "Completed".equalsIgnoreCase(status)
                ? holder.binding.getRoot().getResources().getColor(R.color.green)
                : ("Cancelled".equalsIgnoreCase(status)
                ? holder.binding.getRoot().getResources().getColor(R.color.red)
                : holder.binding.getRoot().getResources().getColor(R.color.colorPrimary));
        holder.binding.tvStatus.setTextColor(color);
        holder.binding.tripId.setText(safe(item.getOrderId()));
    }

    private static String safe(String s) {
        return s == null ? "-" : s;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final BookingCardViewHolderBinding binding;

        Holder(BookingCardViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
