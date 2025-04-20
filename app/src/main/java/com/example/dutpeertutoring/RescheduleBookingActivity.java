package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RescheduleBookingActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button rescheduleButton;
    private String bookingId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule_booking);

        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        rescheduleButton = findViewById(R.id.rescheduleButton);
        firestore = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("bookingId");

        rescheduleButton.setOnClickListener(v -> rescheduleBooking());
    }

    private void rescheduleBooking() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String newDateTime = day + "/" + month + "/" + year + " " + hour + ":" + minute;

        Map<String, Object> updates = new HashMap<>();
        updates.put("dateTime", newDateTime);
        updates.put("status", "Rescheduled");

        firestore.collection("bookings").document(bookingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking rescheduled successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reschedule booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}