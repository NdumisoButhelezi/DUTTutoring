package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements TutorAdapter.OnTutorActionListener {

    private RecyclerView unapprovedTutorsRecyclerView, approvedTutorsRecyclerView;
    private TutorAdapter unapprovedTutorAdapter, approvedTutorAdapter;
    private List<Tutor> unapprovedTutorsList, approvedTutorsList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Initialize RecyclerViews
        unapprovedTutorsRecyclerView = findViewById(R.id.unapprovedTutorsRecyclerView);
        approvedTutorsRecyclerView = findViewById(R.id.approvedTutorsRecyclerView);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        unapprovedTutorsList = new ArrayList<>();
        approvedTutorsList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Find Buttons
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnAnalytics = findViewById(R.id.btnAnalytics);

        // Set up RecyclerViews
        unapprovedTutorAdapter = new TutorAdapter(unapprovedTutorsList, this, this);
        approvedTutorAdapter = new TutorAdapter(approvedTutorsList, this, this);

        unapprovedTutorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        unapprovedTutorsRecyclerView.setAdapter(unapprovedTutorAdapter);

        approvedTutorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        approvedTutorsRecyclerView.setAdapter(approvedTutorAdapter);

        // Set Logout Button Click Listener
        btnLogout.setOnClickListener(v -> {
            auth.signOut(); // Log the user out
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close the activity
        });

        // Set Click Listener for Analytics Navigation
        btnAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAnalyticsActivity.class);
            startActivity(intent);
        });

        // Fetch tutors
        fetchTutors();
    }

    private void fetchTutors() {
        firestore.collection("users")
                .whereEqualTo("role", "Tutor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    unapprovedTutorsList.clear();
                    approvedTutorsList.clear();

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        Tutor tutor = documentSnapshot.toObject(Tutor.class);
                        if (tutor != null) {
                            tutor.setId(documentSnapshot.getId()); // Set the document ID
                            if (Boolean.TRUE.equals(tutor.isApproved())) { // Approved tutors
                                approvedTutorsList.add(tutor);
                            } else { // Unapproved tutors
                                unapprovedTutorsList.add(tutor);
                            }
                        }
                    }

                    unapprovedTutorAdapter.notifyDataSetChanged();
                    approvedTutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onApproveTutor(Tutor tutor) {
        if (tutor.getId() == null || tutor.getId().isEmpty()) {
            Toast.makeText(this, "Cannot approve tutor: Missing document ID", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(tutor.getId())
                .update("approved", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Approved: " + tutor.getName(), Toast.LENGTH_SHORT).show();
                    unapprovedTutorsList.remove(tutor);
                    tutor.setApproved(true);
                    if (!approvedTutorsList.contains(tutor)) {
                        approvedTutorsList.add(tutor);
                    }
                    unapprovedTutorAdapter.notifyDataSetChanged();
                    approvedTutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to approve tutor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRejectTutor(Tutor tutor) {
        if (tutor.getId() == null || tutor.getId().isEmpty()) {
            Toast.makeText(this, "Cannot reject tutor: Missing document ID", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(tutor.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rejected: " + tutor.getName(), Toast.LENGTH_SHORT).show();
                    unapprovedTutorsList.remove(tutor);
                    unapprovedTutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject tutor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}