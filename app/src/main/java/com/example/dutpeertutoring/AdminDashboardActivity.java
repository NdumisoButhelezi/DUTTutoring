package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView tutorsListView;
    private Button refreshButton;
    private FirebaseFirestore firestore;
    private List<Tutor> tutorsList;
    private TutorAdapter tutorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tutorsListView = findViewById(R.id.tutorsListView);
        refreshButton = findViewById(R.id.refreshButton);
        firestore = FirebaseFirestore.getInstance();
        tutorsList = new ArrayList<>();
        tutorAdapter = new TutorAdapter(this, tutorsList);

        tutorsListView.setAdapter(tutorAdapter);

        refreshButton.setOnClickListener(v -> fetchUnconfirmedTutors());
    }

    private void fetchUnconfirmedTutors() {
        firestore.collection("users")
                .whereEqualTo("isConfirmed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tutorsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tutor tutor = document.toObject(Tutor.class);
                        tutor.setId(document.getId());
                        tutorsList.add(tutor);
                    }
                    tutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void confirmTutor(String tutorId) {
        firestore.collection("users").document(tutorId)
                .update("isConfirmed", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tutor confirmed successfully!", Toast.LENGTH_SHORT).show();
                    fetchUnconfirmedTutors();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to confirm tutor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}