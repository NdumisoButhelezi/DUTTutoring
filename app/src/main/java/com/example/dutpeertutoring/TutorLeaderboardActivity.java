package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TutorLeaderboardActivity extends AppCompatActivity {

    private RecyclerView leaderboardRecyclerView;
    private TutorLeaderboardAdapter tutorAdapter;
    private List<Tutor> ratedTutors;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ratedTutors = new ArrayList<>();

        tutorAdapter = new TutorLeaderboardAdapter(ratedTutors, this);
        leaderboardRecyclerView.setAdapter(tutorAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Fetch tutors by default
        fetchTutors();
    }

    private void fetchTutors() {
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
                    tutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}