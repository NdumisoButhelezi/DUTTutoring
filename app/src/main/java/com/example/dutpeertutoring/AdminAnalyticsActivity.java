package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private TextView totalTutorsText, totalStudentsText, totalBookingsText, averageRatingText;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        totalTutorsText = findViewById(R.id.totalTutorsText);
        totalStudentsText = findViewById(R.id.totalStudentsText);
        totalBookingsText = findViewById(R.id.totalBookingsText);
        averageRatingText = findViewById(R.id.averageRatingText);
        firestore = FirebaseFirestore.getInstance();

        fetchAnalytics();
    }

    private void fetchAnalytics() {
        firestore.collection("users").whereEqualTo("role", "Tutor").get()
                .addOnSuccessListener(queryDocumentSnapshots -> totalTutorsText.setText("Total Tutors: " + queryDocumentSnapshots.size()));

        firestore.collection("users").whereEqualTo("role", "Student").get()
                .addOnSuccessListener(queryDocumentSnapshots -> totalStudentsText.setText("Total Students: " + queryDocumentSnapshots.size()));

        firestore.collection("bookings").get()
                .addOnSuccessListener(queryDocumentSnapshots -> totalBookingsText.setText("Total Bookings: " + queryDocumentSnapshots.size()));

        firestore.collection("reviews").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = 0;
                    for (var document : queryDocumentSnapshots) {
                        totalRating += document.getDouble("rating");
                    }
                    double averageRating = totalRating / queryDocumentSnapshots.size();
                    averageRatingText.setText("Average Rating: " + averageRating);
                });
    }
}