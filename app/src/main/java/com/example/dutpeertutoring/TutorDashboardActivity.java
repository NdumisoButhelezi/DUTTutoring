package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "TutorDashboardActivity";

    private ListView bookingsListView;
    private Spinner bookingStatusSpinner;
    private ImageButton refreshButton, sessionButton, leaderboardButton;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private final String[] bookingStatuses = {"Pending", "Approved:WaitingPayment", "Approved:Paid", "Cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        Log.d(TAG, "onCreate");

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        bookingsListView = findViewById(R.id.bookingsListView);
        bookingStatusSpinner = findViewById(R.id.bookingStatusSpinner);
        refreshButton = findViewById(R.id.refreshButton);
        sessionButton = findViewById(R.id.sessionButton);
        leaderboardButton = findViewById(R.id.leaderboardButton);

        ImageButton btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bookingStatuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookingStatusSpinner.setAdapter(spinnerAdapter);

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

        fetchBookingsByStatus(bookingStatuses[0]);

        bookingStatusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                fetchBookingsByStatus(bookingStatuses[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        refreshButton.setOnClickListener(v -> {
            int pos = bookingStatusSpinner.getSelectedItemPosition();
            fetchBookingsByStatus(bookingStatuses[pos]);
        });

        sessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, TutorSessionActivity.class);
            startActivity(intent);
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}