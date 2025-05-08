package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

    private RecyclerView bookingsRecycler;
    private StudentBookingsAdapter adapter;
    private List<Booking> waitingPaymentBookings;
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

        // Initialize RecyclerView and adapter for bookings
        bookingsRecycler = findViewById(R.id.bookingsRecycler);
        bookingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        waitingPaymentBookings = new ArrayList<>();
        adapter = new StudentBookingsAdapter(waitingPaymentBookings, this);
        bookingsRecycler.setAdapter(adapter);

        // Initialize Firebase Firestore and student id
        firestore = FirebaseFirestore.getInstance();
        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch accepted bookings from Firestore
        fetchAcceptedBookings();

        // Floating Action Button to access resources
        FloatingActionButton fab = findViewById(R.id.fabResources);
        fab.setOnClickListener(view -> {
            // Navigate to the StudentResourcesActivity
            startActivity(new Intent(AcceptedBookingsActivity.this, StudentResourcesActivity.class));
        });
    }

    private void fetchAcceptedBookings() {
        firestore.collection("bookings")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("status", "Approved:WaitingPayment")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    waitingPaymentBookings.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());
                        waitingPaymentBookings.add(booking);
                    }
                    adapter.notifyDataSetChanged();
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
            Snackbar.make(bookingsRecycler, "Payment failed! Please try again.", Snackbar.LENGTH_LONG).show();
            return;
        }
        firestore.collection("bookings")
                .document(booking.getId())
                .update("status", "Approved:Paid", "paid", true)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(bookingsRecycler, "Payment successful!", Snackbar.LENGTH_LONG).show();
                    // Redirect to the resources page after successful payment.
                    startActivity(new Intent(AcceptedBookingsActivity.this, StudentResourcesActivity.class));
                    fetchAcceptedBookings();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(bookingsRecycler, "Payment update failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to Dashboard
            startActivity(new Intent(this, StudentDashboardActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}