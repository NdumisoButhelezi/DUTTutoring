package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView unconfirmedTutorsListView, confirmedTutorsListView;
    private TutorAdapter unconfirmedTutorAdapter, confirmedTutorAdapter;
    private List<Tutor> unconfirmedTutorsList, confirmedTutorsList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        unconfirmedTutorsListView = findViewById(R.id.unconfirmedTutorsListView);
        confirmedTutorsListView = findViewById(R.id.confirmedTutorsListView);

        firestore = FirebaseFirestore.getInstance();
        unconfirmedTutorsList = new ArrayList<>();
        confirmedTutorsList = new ArrayList<>();

        // Initialize adapters
        unconfirmedTutorAdapter = new TutorAdapter(this, unconfirmedTutorsList);
        confirmedTutorAdapter = new TutorAdapter(this, confirmedTutorsList);

        // Set adapters to ListViews
        unconfirmedTutorsListView.setAdapter(unconfirmedTutorAdapter);
        confirmedTutorsListView.setAdapter(confirmedTutorAdapter);

        // Fetch tutors
        fetchUnconfirmedTutors();
        fetchConfirmedTutors();

        // Handle click events for unconfirmed tutors
        unconfirmedTutorsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Tutor selectedTutor = unconfirmedTutorsList.get(position);
            Intent intent = new Intent(AdminDashboardActivity.this, TutorDetailActivity.class);
            intent.putExtra("tutorId", selectedTutor.getId());
            startActivity(intent);
        });

        // Handle click events for confirmed tutors
        confirmedTutorsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Tutor selectedTutor = confirmedTutorsList.get(position);
            Intent intent = new Intent(AdminDashboardActivity.this, TutorDetailActivity.class);
            intent.putExtra("tutorId", selectedTutor.getId());
            startActivity(intent);
        });
    }

    private void fetchUnconfirmedTutors() {
        firestore.collection("users")
                .whereEqualTo("isConfirmed", false)
                .whereEqualTo("profileComplete", true) // Only fetch tutors with complete profiles
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    unconfirmedTutorsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tutor tutor = document.toObject(Tutor.class);
                        tutor.setId(document.getId());
                        unconfirmedTutorsList.add(tutor);
                    }
                    unconfirmedTutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch unconfirmed tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchConfirmedTutors() {
        firestore.collection("users")
                .whereEqualTo("isConfirmed", true) // Fetch only confirmed tutors
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    confirmedTutorsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tutor tutor = document.toObject(Tutor.class);
                        tutor.setId(document.getId());
                        confirmedTutorsList.add(tutor);
                    }
                    confirmedTutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch confirmed tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}