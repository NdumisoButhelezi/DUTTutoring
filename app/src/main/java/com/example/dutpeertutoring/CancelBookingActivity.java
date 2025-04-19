package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class CancelBookingActivity extends AppCompatActivity {

    private Button cancelButton;
    private String bookingId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_booking);

        cancelButton = findViewById(R.id.cancelButton);
        firestore = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("bookingId");

        cancelButton.setOnClickListener(v -> cancelBooking());
    }

    private void cancelBooking() {
        firestore.collection("bookings").document(bookingId)
                .update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking cancelled successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}