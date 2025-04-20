package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView tutorsListView;
    private TutorAdapter tutorAdapter;
    private List<Tutor> tutorsList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tutorsListView = findViewById(R.id.tutorsListView);
        firestore = FirebaseFirestore.getInstance();
        tutorsList = new ArrayList<>();

        // Initialize the adapter with the list and context
        tutorAdapter = new TutorAdapter(tutorsList, this);
        tutorsListView.setAdapter(tutorAdapter);

        fetchUnconfirmedTutors();
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
}