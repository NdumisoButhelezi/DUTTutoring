package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
    private TutorAdapter tutorAdapter;
    private FirebaseFirestore firestore;
    private List<Tutor> tutorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        cardStackView = findViewById(R.id.cardStackView);
        cardStackLayoutManager = new CardStackLayoutManager(this, this);
        cardStackView.setLayoutManager(cardStackLayoutManager);

        firestore = FirebaseFirestore.getInstance();
        tutorList = new ArrayList<>();
        tutorAdapter = new TutorAdapter(tutorList, this);
        cardStackView.setAdapter(tutorAdapter);

        fetchTutors();
    }

    private void fetchTutors() {
        firestore.collection("users")
                .whereEqualTo("isConfirmed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tutorList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tutor tutor = document.toObject(Tutor.class);
                        tutor.setId(document.getId());
                        tutorList.add(tutor);
                    }
                    tutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (direction == Direction.Left) {
            // Reject the tutor
            Tutor rejectedTutor = tutorList.get(cardStackLayoutManager.getTopPosition() - 1);
            String studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> rejection = new HashMap<>();
            rejection.put("studentId", studentId);
            rejection.put("tutorId", rejectedTutor.getId());
            rejection.put("status", "Rejected");

            firestore.collection("rejections").add(rejection)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Tutor rejected!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to reject tutor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else if (direction == Direction.Right) {
            // Open the booking page
            Tutor selectedTutor = tutorList.get(cardStackLayoutManager.getTopPosition() - 1);
            Intent intent = new Intent(this, BookingPageActivity.class);
            intent.putExtra("tutorId", selectedTutor.getId());
            intent.putExtra("tutorName", selectedTutor.getName());
            intent.putExtra("tutorModules", new ArrayList<>(selectedTutor.getModules()));
            startActivity(intent);
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) { }

    @Override
    public void onCardRewound() { }

    @Override
    public void onCardCanceled() { }

    @Override
    public void onCardAppeared(int position, Object object) { }

    @Override
    public void onCardDisappeared(int position, Object object) { }
}