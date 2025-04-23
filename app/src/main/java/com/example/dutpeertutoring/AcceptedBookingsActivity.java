package com.example.dutpeertutoring;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        bookingsRecycler = findViewById(R.id.bookingsRecycler);
        bookingsRecycler.setLayoutManager(new LinearLayoutManager(this));

        waitingPaymentBookings = new ArrayList<>();
        adapter = new StudentBookingsAdapter(waitingPaymentBookings, this);
        bookingsRecycler.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchAcceptedBookings();
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
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onPay(Booking booking) {
        new AlertDialog.Builder(this)
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
                    fetchAcceptedBookings();
                    // Optional: Also update Tutor dashboard in real-time if using listeners
                })
                .addOnFailureListener(e -> Snackbar.make(bookingsRecycler, "Payment update failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }
}