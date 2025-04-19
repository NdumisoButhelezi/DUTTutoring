package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TutorDashboardActivity extends AppCompatActivity {

    private ListView bookingsListView;
    private FirebaseFirestore firestore;
    private String tutorId;
    private List<Booking> bookingsList;
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        bookingsListView = findViewById(R.id.bookingsListView);
        firestore = FirebaseFirestore.getInstance();
        tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingsList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(this, bookingsList);

        bookingsListView.setAdapter(bookingAdapter);

        fetchBookings();
    }

    private void fetchBookings() {
        firestore.collection("bookings")
                .whereEqualTo("tutorId", tutorId)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Booking booking = document.toObject(Booking.class);
                        booking.setId(document.getId());
                        bookingsList.add(booking);
                    }
                    bookingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void approveBooking(String bookingId) {
        updateBookingStatus(bookingId, "Approved");
    }

    public void rejectBooking(String bookingId) {
        updateBookingStatus(bookingId, "Rejected");
    }

    private void updateBookingStatus(String bookingId, String status) {
        firestore.collection("bookings").document(bookingId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking " + status.toLowerCase() + " successfully!", Toast.LENGTH_SHORT).show();
                    fetchBookings();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}