package com.example.dutpeertutoring;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentResourcesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ResourcesAdapter adapter;
    private FirebaseFirestore firestore;
    private Button btnSaveRatings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_resources);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResourcesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        fetchHistoricalBookings();

        // Save Ratings Button
        btnSaveRatings = findViewById(R.id.btnSaveRatings);
        btnSaveRatings.setOnClickListener(v -> {
            // Save all ratings (if applicable)
            adapter.saveRatingsToFirestore();
            Toast.makeText(this, "Ratings saved successfully!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Fetch tutors associated with historical (paid) bookings.
     */
    private void fetchHistoricalBookings() {
        firestore.collection("bookings")
                .whereEqualTo("studentId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereEqualTo("status", "Approved:Paid")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> tutorIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tutorId = doc.getString("tutorId");
                        if (tutorId != null && !tutorIds.contains(tutorId)) {
                            tutorIds.add(tutorId);
                        }
                    }
                    fetchResourcesForTutors(tutorIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load historical bookings", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetch resources uploaded by tutors who are linked to historical bookings.
     */
    private void fetchResourcesForTutors(List<String> tutorIds) {
        firestore.collection("resources")
                .whereIn("tutorId", tutorIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Resource> resources = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Resource resource = doc.toObject(Resource.class);
                        resource.setId(doc.getId());
                        resources.add(resource);
                    }
                    adapter.updateResources(resources);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load resources", Toast.LENGTH_SHORT).show();
                });
    }
}