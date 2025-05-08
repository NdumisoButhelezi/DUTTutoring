package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
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
        fetchResources();

        // Save Ratings Button
        btnSaveRatings = findViewById(R.id.btnSaveRatings);
        btnSaveRatings.setOnClickListener(v -> {
            // Save all ratings (if applicable)
            adapter.saveRatingsToFirestore();
            Toast.makeText(this, "Ratings saved successfully!", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchResources() {
        firestore.collection("resources").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Resource> resources = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
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