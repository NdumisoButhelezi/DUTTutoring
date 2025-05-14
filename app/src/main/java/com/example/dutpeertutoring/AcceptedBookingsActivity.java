package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AcceptedBookingsActivity extends AppCompatActivity implements StudentBookingsAdapter.OnBookingPayListener {

    private RecyclerView acceptedRecycler, historyRecycler;
    private StudentBookingsAdapter acceptedAdapter, historyAdapter;
    private List<Booking> acceptedBookings, historyBookings;
    private FirebaseFirestore firestore;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_bookings);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Accepted Bookings");
        }

        // Initialize RecyclerViews
        acceptedRecycler = findViewById(R.id.acceptedBookingsRecycler);
        historyRecycler = findViewById(R.id.historyBookingsRecycler);

        acceptedRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Initialize booking lists and adapters
        acceptedBookings = new ArrayList<>();
        historyBookings = new ArrayList<>();

        acceptedAdapter = new StudentBookingsAdapter(acceptedBookings, this, true); // Show "Pay" button
        historyAdapter = new StudentBookingsAdapter(historyBookings, null, false); // No "Pay" button

        acceptedRecycler.setAdapter(acceptedAdapter);
        historyRecycler.setAdapter(historyAdapter);

        // Initialize Firebase Firestore and student ID
        firestore = FirebaseFirestore.getInstance();
        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch bookings
        fetchBookings();

        // Floating buttons
        FloatingActionButton fabResources = findViewById(R.id.fabResources);
        fabResources.setOnClickListener(view -> {
            startActivity(new Intent(AcceptedBookingsActivity.this, StudentResourcesActivity.class));
        });

        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(view -> {
            Intent intent = new Intent(AcceptedBookingsActivity.this, MessagingActivity.class);
            intent.putExtra("bookingId", "defaultBookingId");
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });
    }

    private void fetchBookings() {
        firestore.collection("bookings")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    acceptedBookings.clear();
                    historyBookings.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());

                        if ("Approved:WaitingPayment".equals(booking.getStatus()) && !booking.isPaid()) {
                            acceptedBookings.add(booking);
                        } else if ("Approved:Paid".equals(booking.getStatus())) {
                            historyBookings.add(booking);
                        }
                    }
                    acceptedAdapter.notifyDataSetChanged();
                    historyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onPay(Booking booking) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Simulate Payment")
                .setMessage("Choose payment result for this booking:")
                .setPositiveButton("Valid Payment", (dialog, which) -> processPayment(booking, true))
                .setNegativeButton("Invalid Payment", (dialog, which) -> processPayment(booking, false))
                .show();
    }

    private void processPayment(Booking booking, boolean valid) {
        if (!valid) {
            Snackbar.make(acceptedRecycler, "Payment failed! Please try again.", Snackbar.LENGTH_LONG).show();
            return;
        }
        firestore.collection("bookings")
                .document(booking.getId())
                .update("status", "Approved:Paid", "paid", true)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(acceptedRecycler, "Payment successful!", Snackbar.LENGTH_LONG).show();
                    fetchBookings();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(acceptedRecycler, "Payment update failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, StudentDashboardActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}