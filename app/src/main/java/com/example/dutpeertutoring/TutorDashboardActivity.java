package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TutorDashboardActivity extends AppCompatActivity {

    private ListView bookingsListView;
    private Spinner bookingStatusSpinner;
    private ImageButton refreshButton, sessionButton, leaderboardButton;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    // Available booking statuses to filter
    private final String[] bookingStatuses = {"Pending", "Approved:WaitingPayment", "Approved:Paid", "Cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views from layout.
        // Ensure that your layout includes views with ids: bookingsListView, bookingStatusSpinner, refreshButton, sessionButton, leaderboardButton
        bookingsListView = findViewById(R.id.bookingsListView);
        bookingStatusSpinner = findViewById(R.id.bookingStatusSpinner);
        refreshButton = findViewById(R.id.refreshButton);
        sessionButton = findViewById(R.id.sessionButton); // new button to start session/upload resources
        leaderboardButton = findViewById(R.id.leaderboardButton); // new button to view tutor leaderboard

        // Setup Spinner adapter for booking statuses
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bookingStatuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookingStatusSpinner.setAdapter(spinnerAdapter);

        // Setup booking list and adapter (using an existing BookingAdapter)
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(this, bookingList, new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onApprove(Booking booking) {
                approveBooking(booking);
            }
            @Override
            public void onDecline(Booking booking) {
                declineBooking(booking);
            }
        });
        bookingsListView.setAdapter(bookingAdapter);

        // Fetch bookings for the initially selected status
        fetchBookingsByStatus(bookingStatuses[0]);

        bookingStatusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                fetchBookingsByStatus(bookingStatuses[position]);
                // If the selected status is "Approved:Paid", show the sessionButton; otherwise hide it.
                if (bookingStatuses[position].equals("Approved:Paid")) {
                    sessionButton.setVisibility(View.VISIBLE);
                } else {
                    sessionButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // Refresh button action to refetch bookings for the currently selected status
        refreshButton.setOnClickListener(v -> {
            int pos = bookingStatusSpinner.getSelectedItemPosition();
            fetchBookingsByStatus(bookingStatuses[pos]);
        });

        // sessionButton action to navigate to the TutorSessionActivity where tutor can upload resources
        sessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, TutorSessionActivity.class);
            startActivity(intent);
        });

        // leaderboardButton action to navigate to TutorLeaderboardActivity to view tutor ratings leaderboard
        leaderboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, TutorLeaderboardActivity.class);
            startActivity(intent);
        });
    }

    private void fetchBookingsByStatus(String status) {
        String tutorId = auth.getCurrentUser().getUid();
        firestore.collection("bookings")
                .whereEqualTo("tutorId", tutorId)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Booking booking = document.toObject(Booking.class);
                        booking.setId(document.getId());
                        bookingList.add(booking);
                    }
                    bookingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TutorDashboardActivity.this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void approveBooking(Booking booking) {
        firestore.collection("bookings")
                .document(booking.getId())
                .update("status", "Approved:WaitingPayment")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TutorDashboardActivity.this, "Booking approved!", Toast.LENGTH_SHORT).show();
                    int pos = bookingStatusSpinner.getSelectedItemPosition();
                    fetchBookingsByStatus(bookingStatuses[pos]);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TutorDashboardActivity.this, "Failed to approve: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void declineBooking(Booking booking) {
        firestore.collection("bookings")
                .document(booking.getId())
                .update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TutorDashboardActivity.this, "Booking declined!", Toast.LENGTH_SHORT).show();
                    int pos = bookingStatusSpinner.getSelectedItemPosition();
                    fetchBookingsByStatus(bookingStatuses[pos]);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TutorDashboardActivity.this, "Failed to decline: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}