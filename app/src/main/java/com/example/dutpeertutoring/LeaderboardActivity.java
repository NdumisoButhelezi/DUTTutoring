package com.example.dutpeertutoring;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {
    private RecyclerView leaderboardRecyclerView; // Updated variable name
    private LeaderboardAdapter leaderboardAdapter;
    private FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView); // Updated to match XML ID
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter(new ArrayList<>());
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        firestore = FirebaseFirestore.getInstance();
        fetchTutorRatings();
    }

    private void fetchTutorRatings() {
        firestore.collection("ratings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, List<Float>> tutorRatingsMap = new HashMap<>();

                    // Aggregate ratings for each tutor
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tutorId = doc.getString("tutorId");
                        float rating = Float.parseFloat(doc.get("rating").toString());
                        if (!tutorRatingsMap.containsKey(tutorId)) {
                            tutorRatingsMap.put(tutorId, new ArrayList<>());
                        }
                        tutorRatingsMap.get(tutorId).add(rating);
                    }

                    // Calculate average rating for each tutor
                    List<Tutor> tutorList = new ArrayList<>();
                    for (Map.Entry<String, List<Float>> entry : tutorRatingsMap.entrySet()) {
                        String tutorId = entry.getKey();
                        List<Float> ratings = entry.getValue();
                        float sum = 0;
                        for (float r : ratings) {
                            sum += r;
                        }
                        float average = sum / ratings.size();
                        tutorList.add(new Tutor(tutorId, average));
                    }

                    // Sort tutors by average rating in descending order
                    tutorList.sort((t1, t2) -> Float.compare(t2.getAverageRating(), t1.getAverageRating()));

                    // Update RecyclerView
                    leaderboardAdapter.updateTutors(tutorList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                });
    }
}