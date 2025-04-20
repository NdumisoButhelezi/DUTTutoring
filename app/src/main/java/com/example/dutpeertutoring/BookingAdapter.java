package com.example.dutpeertutoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class BookingAdapter extends BaseAdapter {

    private final Context context;
    private final List<Booking> bookings;

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
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
        TextView dateTime = convertView.findViewById(R.id.dateTime);
        TextView status = convertView.findViewById(R.id.statusText);

        moduleName.setText("Module: " + booking.getModule());
        dateTime.setText("Date & Time: " + booking.getDateTime());
        status.setText("Status: " + booking.getStatus());

        return convertView;
    }
}