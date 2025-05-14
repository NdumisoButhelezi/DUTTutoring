package com.example.dutpeertutoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentBookingsAdapter extends RecyclerView.Adapter<StudentBookingsAdapter.BookingViewHolder> {

    public interface OnBookingPayListener {
        void onPay(Booking booking);
    }

    private final List<Booking> bookings;
    private final OnBookingPayListener listener;
    private final boolean showPayButton; // Flag to indicate if "Pay" button should be shown

    public StudentBookingsAdapter(List<Booking> bookings, OnBookingPayListener listener, boolean showPayButton) {
        this.bookings = bookings;
        this.listener = listener;
        this.showPayButton = showPayButton;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_booking, parent, false);
        return new BookingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking b = bookings.get(position);
        holder.module.setText("Module: " + b.getModule());
        holder.date.setText("Date: " + b.getDate());
        holder.time.setText("Time: " + b.getStartTime() + " - " + b.getEndTime());
        holder.status.setText("Status: " + b.getStatus());

        if (showPayButton && "Approved:WaitingPayment".equals(b.getStatus()) && !b.isPaid()) {
            holder.payButton.setVisibility(View.VISIBLE);
            holder.payButton.setOnClickListener(v -> listener.onPay(b));
        } else {
            holder.payButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView module, date, time, status;
        Button payButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            module = itemView.findViewById(R.id.moduleName);
            date = itemView.findViewById(R.id.dateText);
            time = itemView.findViewById(R.id.timesText);
            status = itemView.findViewById(R.id.statusText);
            payButton = itemView.findViewById(R.id.payButton);
        }
    }
}