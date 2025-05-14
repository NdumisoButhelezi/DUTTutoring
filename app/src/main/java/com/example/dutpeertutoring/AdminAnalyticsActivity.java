package com.example.dutpeertutoring;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private TextView totalTutorsText, totalStudentsText, totalBookingsText, averageRatingText;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setTitle("Admin Analytics"); // Set toolbar title
        }

        // Initialize TextViews
        totalTutorsText = findViewById(R.id.totalTutorsText);
        totalStudentsText = findViewById(R.id.totalStudentsText);
        totalBookingsText = findViewById(R.id.totalBookingsText);
        averageRatingText = findViewById(R.id.averageRatingText);
        firestore = FirebaseFirestore.getInstance();

        // Fetch analytics data
        fetchAnalytics();
    }

    private void fetchAnalytics() {
        // Fetch total tutors
        firestore.collection("users").whereEqualTo("role", "Tutor").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalTutors = queryDocumentSnapshots.size();
                    totalTutorsText.setText("Total Tutors: " + totalTutors);
                });

        // Fetch total students
        firestore.collection("users").whereEqualTo("role", "Student").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalStudents = queryDocumentSnapshots.size();
                    totalStudentsText.setText("Total Students: " + totalStudents);
                });

        // Fetch total bookings
        firestore.collection("bookings").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalBookings = queryDocumentSnapshots.size();
                    totalBookingsText.setText("Total Bookings: " + totalBookings);
                });

        // Fetch average rating
        firestore.collection("reviews").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = 0;
                    for (var document : queryDocumentSnapshots) {
                        totalRating += document.getDouble("rating");
                    }
                    double averageRating = queryDocumentSnapshots.size() > 0 ? totalRating / queryDocumentSnapshots.size() : 0;
                    averageRatingText.setText("Average Rating: " + String.format("%.2f", averageRating));
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to AdminDashboardActivity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}