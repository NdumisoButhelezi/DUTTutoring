package com.example.dutpeertutoring;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows all bookings by the student, allows simulated payment for unpaid bookings.
 */
public class StudentBookingsActivity extends AppCompatActivity
        implements StudentBookingsAdapter.OnBookingPayListener {

    private RecyclerView bookingsRecycler;
    private StudentBookingsAdapter adapter;
    private List<Booking> allBookings;
    private FirebaseFirestore firestore;
    private String studentId;

    private TabLayout tabLayout;
    private List<Booking> filteredBookings = new ArrayList<>();
    private static final int TAB_PENDING = 0, TAB_CONFIRMED = 1, TAB_CANCELLED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_bookings);

        tabLayout = findViewById(R.id.tabLayout);
        bookingsRecycler = findViewById(R.id.bookingsRecycler);
        bookingsRecycler.setLayoutManager(new LinearLayoutManager(this));

        allBookings = new ArrayList<>();
        adapter = new StudentBookingsAdapter(filteredBookings, this);
        bookingsRecycler.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Confirmed"));
        tabLayout.addTab(tabLayout.newTab().setText("Cancelled"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { filterBookings(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { filterBookings(); }
        });

        fetchBookings();
    }

    private void fetchBookings() {
        firestore.collection("bookings")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allBookings.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());
                        allBookings.add(booking);
                    }
                    filterBookings();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void filterBookings() {
        int selectedTab = tabLayout.getSelectedTabPosition();
        filteredBookings.clear();
        for (Booking b : allBookings) {
            switch (selectedTab) {
                case TAB_PENDING:
                    if ("Pending".equals(b.getStatus()) || "Approved:WaitingPayment".equals(b.getStatus()))
                        filteredBookings.add(b);
                    break;
                case TAB_CONFIRMED:
                    if ("Approved:Paid".equals(b.getStatus()))
                        filteredBookings.add(b);
                    break;
                case TAB_CANCELLED:
                    if ("Cancelled".equals(b.getStatus()))
                        filteredBookings.add(b);
                    break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPay(Booking booking) {
        // Simulate payment: show dialog for "valid" or "invalid"
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
                    fetchBookings();
                })
                .addOnFailureListener(e -> Snackbar.make(bookingsRecycler, "Payment update failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }
}