package com.example.dutpeertutoring;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class BookingAdapter extends BaseAdapter {

    public interface OnBookingActionListener {
        void onApprove(Booking booking);
        void onDecline(Booking booking);
    }

    private final Context context;
    private final List<Booking> bookings;
    private final OnBookingActionListener actionListener;

    // actionListener can be null for lists where actions are not needed
    public BookingAdapter(Context context, List<Booking> bookings, OnBookingActionListener actionListener) {
        this.context = context;
        this.bookings = bookings;
        this.actionListener = actionListener;
    }

    @Override
    public int getCount() {
        return bookings.size();
    }

    @Override
    public Object getItem(int position) {
        return bookings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        }

        Booking booking = bookings.get(position);

        TextView moduleName = convertView.findViewById(R.id.moduleName);
        TextView date = convertView.findViewById(R.id.dateText);
        TextView times = convertView.findViewById(R.id.timesText);
        TextView status = convertView.findViewById(R.id.statusText);
        Button approveButton = convertView.findViewById(R.id.approveButton);
        Button declineButton = convertView.findViewById(R.id.declineButton);

        moduleName.setText("Module: " + booking.getModule());
        date.setText("Date: " + booking.getDate());
        times.setText("Time: " + booking.getStartTime() + " - " + booking.getEndTime());
        status.setText("Status: " + booking.getStatus() + (booking.isPaid() ? " (Paid)" : ""));

        // If the booking is confirmed (for example, waiting payment or paid), strike through the time slot.
        if ("Approved:WaitingPayment".equalsIgnoreCase(booking.getStatus()) ||
                "Approved:Paid".equalsIgnoreCase(booking.getStatus())) {
            times.setPaintFlags(times.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // Remove strike-through in case the view is reused
            times.setPaintFlags(times.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Only show the buttons for pending bookings and if a listener is set
        if ("Pending".equalsIgnoreCase(booking.getStatus()) && actionListener != null) {
            approveButton.setVisibility(View.VISIBLE);
            declineButton.setVisibility(View.VISIBLE);

            approveButton.setOnClickListener(v -> actionListener.onApprove(booking));
            declineButton.setOnClickListener(v -> actionListener.onDecline(booking));
        } else {
            approveButton.setVisibility(View.GONE);
            declineButton.setVisibility(View.GONE);
        }

        return convertView;
    }
}