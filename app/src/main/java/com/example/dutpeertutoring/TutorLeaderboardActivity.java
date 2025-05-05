package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TutorLeaderboardActivity extends AppCompatActivity {

    private RecyclerView leaderboardRecyclerView;
    private TutorLeaderboardAdapter adapter;
    private List<Tutor> ratedTutors;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_leaderboard);

        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ratedTutors = new ArrayList<>();
        adapter = new TutorLeaderboardAdapter(ratedTutors, this);
        leaderboardRecyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        fetchRatedTutors();
    }

    private void fetchRatedTutors() {
        firestore.collection("tutors")
                .whereEqualTo("approved", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ratedTutors.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Tutor tutor = doc.toObject(Tutor.class);
                        tutor.setId(doc.getId());
                        ratedTutors.add(tutor);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TutorLeaderboardActivity.this, "Failed to fetch rated tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}