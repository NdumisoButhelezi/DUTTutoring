package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TutorDashboardActivity extends AppCompatActivity {

    private ListView pendingListView, cancelledListView, waitingPaymentListView, paidListView;
    private BookingAdapter pendingAdapter, cancelledAdapter, waitingPaymentAdapter, paidAdapter;
    private List<Booking> pendingBookings, cancelledBookings, waitingPaymentBookings, paidBookings;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        pendingListView = findViewById(R.id.pendingListView);
        cancelledListView = findViewById(R.id.cancelledListView);
        waitingPaymentListView = findViewById(R.id.waitingPaymentListView);
        paidListView = findViewById(R.id.paidListView);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        pendingBookings = new ArrayList<>();
        cancelledBookings = new ArrayList<>();
        waitingPaymentBookings = new ArrayList<>();
        paidBookings = new ArrayList<>();

        // Pass the action listener ONLY for pending bookings
        pendingAdapter = new BookingAdapter(this, pendingBookings, new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onApprove(Booking booking) {
                approveBooking(booking);
            }
            @Override
            public void onDecline(Booking booking) {
                declineBooking(booking);
            }
        });
        // For others, pass null so no buttons show
        cancelledAdapter = new BookingAdapter(this, cancelledBookings, null);
        waitingPaymentAdapter = new BookingAdapter(this, waitingPaymentBookings, null);
        paidAdapter = new BookingAdapter(this, paidBookings, null);

        pendingListView.setAdapter(pendingAdapter);
        cancelledListView.setAdapter(cancelledAdapter);
        waitingPaymentListView.setAdapter(waitingPaymentAdapter);
        paidListView.setAdapter(paidAdapter);

        fetchBookings();
    }

    private void fetchBookings() {
        String tutorId = auth.getCurrentUser().getUid();
        firestore.collection("bookings")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingBookings.clear();
                    cancelledBookings.clear();
                    waitingPaymentBookings.clear();
                    paidBookings.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Booking booking = document.toObject(Booking.class);
                        booking.setId(document.getId());
                        switch (booking.getStatus()) {
                            case "Pending":
                                pendingBookings.add(booking);
                                break;
                            case "Cancelled":
                                cancelledBookings.add(booking);
                                break;
                            case "Approved:WaitingPayment":
                                waitingPaymentBookings.add(booking);
                                break;
                            case "Approved:Paid":
                                paidBookings.add(booking);
                                break;
                        }
                    }
                    pendingAdapter.notifyDataSetChanged();
                    cancelledAdapter.notifyDataSetChanged();
                    waitingPaymentAdapter.notifyDataSetChanged();
                    paidAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void approveBooking(Booking booking) {
        firestore.collection("bookings")
                .document(booking.getId())
                .update("status", "Approved:WaitingPayment")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking approved!", Toast.LENGTH_SHORT).show();
                    fetchBookings();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to approve: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void declineBooking(Booking booking) {
        firestore.collection("bookings")
                .document(booking.getId())
                .update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking declined!", Toast.LENGTH_SHORT).show();
                    fetchBookings();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to decline: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}