package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TutorDashboardActivity extends AppCompatActivity {

    private ListView bookingsListView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingsList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        // Initialize UI components
        bookingsListView = findViewById(R.id.bookingsListView);
        bookingsList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up adapter
        bookingAdapter = new BookingAdapter(this, bookingsList);
        bookingsListView.setAdapter(bookingAdapter);

        // Fetch bookings and listen for updates
        listenForBookings();
    }

    private void listenForBookings() {
        String tutorId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (tutorId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Listen for real-time updates from Firestore
        firestore.collection("bookings")
                .whereEqualTo("tutorId", tutorId) // Fetch bookings specific to the logged-in tutor
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(TutorDashboardActivity.this, "Failed to fetch bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            for (DocumentChange change : value.getDocumentChanges()) {
                                switch (change.getType()) {
                                    case ADDED:
                                        // Add new booking
                                        Booking newBooking = change.getDocument().toObject(Booking.class);
                                        newBooking.setId(change.getDocument().getId());
                                        bookingsList.add(newBooking);
                                        break;

                                    case MODIFIED:
                                        // Update existing booking
                                        String updatedId = change.getDocument().getId();
                                        for (int i = 0; i < bookingsList.size(); i++) {
                                            if (bookingsList.get(i).getId().equals(updatedId)) {
                                                Booking updatedBooking = change.getDocument().toObject(Booking.class);
                                                updatedBooking.setId(updatedId);
                                                bookingsList.set(i, updatedBooking);
                                                break;
                                            }
                                        }
                                        break;

                                    case REMOVED:
                                        // Remove deleted booking
                                        String removedId = change.getDocument().getId();
                                        bookingsList.removeIf(booking -> booking.getId().equals(removedId));
                                        break;
                                }
                            }

                            // Notify adapter about data changes
                            bookingAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}