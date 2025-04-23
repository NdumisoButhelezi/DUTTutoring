package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDashboardActivity extends AppCompatActivity implements CardStackListener {

    private CardStackView cardStackView;
    private CardStackLayoutManager cardStackLayoutManager;
    private CardStackTutorAdapter cardStackTutorAdapter;
    private FirebaseFirestore firestore;
    private List<Tutor> tutorList;
    private FloatingActionButton fabAcceptedBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        cardStackView = findViewById(R.id.cardStackView);
        cardStackLayoutManager = new CardStackLayoutManager(this, this);
        cardStackView.setLayoutManager(cardStackLayoutManager);

        firestore = FirebaseFirestore.getInstance();
        tutorList = new ArrayList<>();
        cardStackTutorAdapter = new CardStackTutorAdapter(tutorList, this);
        cardStackView.setAdapter(cardStackTutorAdapter);

        fabAcceptedBookings = findViewById(R.id.fabAcceptedBookings);
        fabAcceptedBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, AcceptedBookingsActivity.class);
            startActivity(intent);
        });

        fetchTutors();
    }

    private void fetchTutors() {
        Toast.makeText(this, "Loading tutors...", Toast.LENGTH_SHORT).show();

        firestore.collection("users")
                .whereEqualTo("isConfirmed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tutorList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tutor tutor = document.toObject(Tutor.class);
                        tutor.setId(document.getId());
                        tutorList.add(tutor);
                    }
                    if (tutorList.isEmpty()) {
                        Toast.makeText(this, "No tutors available at the moment.", Toast.LENGTH_SHORT).show();
                    }
                    cardStackTutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (tutorList.isEmpty()) {
            return;
        }
        Tutor swipedTutor = tutorList.get(cardStackLayoutManager.getTopPosition() - 1);

        if (direction == Direction.Left) {
            // Reject the tutor
            String studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> rejection = new HashMap<>();
            rejection.put("studentId", studentId);
            rejection.put("tutorId", swipedTutor.getId());
            rejection.put("status", "Rejected");

            firestore.collection("rejections").add(rejection)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Tutor rejected!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to reject tutor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else if (direction == Direction.Right) {
            Intent intent = new Intent(this, BookingPageActivity.class);
            intent.putExtra("tutorId", swipedTutor.getId());
            intent.putExtra("tutorName", swipedTutor.getName());
            intent.putExtra("tutorModules", new ArrayList<>(swipedTutor.getModules()));
            startActivity(intent);
        }

        tutorList.remove(swipedTutor);
        cardStackTutorAdapter.notifyDataSetChanged();
    }

    @Override public void onCardDragging(Direction direction, float ratio) { }
    @Override public void onCardRewound() { }
    @Override public void onCardCanceled() { }
    @Override public void onCardAppeared(android.view.View view, int position) { }
    @Override public void onCardDisappeared(android.view.View view, int position) { }
}