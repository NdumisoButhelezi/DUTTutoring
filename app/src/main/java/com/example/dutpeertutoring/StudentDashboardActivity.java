package com.example.dutpeertutoring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;

import java.util.ArrayList;
import java.util.List;

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
        firestore.collection("users")
                .whereEqualTo("role", "Tutor")
                .whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tutorList.clear();

                    // Iterate through QueryDocumentSnapshot
                    for (QueryDocumentSnapshot tutorDoc : queryDocumentSnapshots) {
                        Tutor tutor = new Tutor();
                        tutor.setId(tutorDoc.getId()); // Get the document ID
                        tutor.setName(tutorDoc.getString("name"));
                        tutor.setModules((List<String>) tutorDoc.get("modules"));
                        tutor.setProfileImageBase64(tutorDoc.getString("profileImageBase64"));
                        tutor.setStatus("Available");

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
            Toast.makeText(this, "Tutor rejected!", Toast.LENGTH_SHORT).show();
        } else if (direction == Direction.Right) {
            // Open the booking page for the selected tutor
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
    @Override public void onCardAppeared(View view, int position) { }
    @Override public void onCardDisappeared(View view, int position) { }
}