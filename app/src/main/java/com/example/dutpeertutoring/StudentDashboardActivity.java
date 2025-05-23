package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Views
        cardStackView = findViewById(R.id.cardStackView);
        progressBar = findViewById(R.id.progressBar);
        cardStackLayoutManager = new CardStackLayoutManager(this, this);
        cardStackView.setLayoutManager(cardStackLayoutManager);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        tutorList = new ArrayList<>();
        cardStackTutorAdapter = new CardStackTutorAdapter(tutorList, this);
        cardStackView.setAdapter(cardStackTutorAdapter);

        // Replace the Logout Button with an ImageButton
        ImageButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            auth.signOut(); // Log the user out
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close the activity
        });

        // FloatingActionButton for accepted bookings
        fabAcceptedBookings = findViewById(R.id.fabAcceptedBookings);
        fabAcceptedBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, AcceptedBookingsActivity.class);
            startActivity(intent);
        });

        // Floating Action Button for Ratings
        FloatingActionButton fabRatings = findViewById(R.id.fabRatings);
        fabRatings.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentResourcesActivity.class);
            startActivity(intent);
        });

        // Fetch tutors
        fetchTutors();
    }

    private void fetchTutors() {
        // Show the ProgressBar while fetching data
        progressBar.setVisibility(View.VISIBLE);
        cardStackView.setVisibility(View.GONE); // Hide the CardStackView until data is fetched

        firestore.collection("users")
                .whereEqualTo("role", "Tutor")
                .whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tutorList.clear();
                    for (DocumentSnapshot tutorDoc : queryDocumentSnapshots.getDocuments()) {
                        Tutor tutor = new Tutor();
                        tutor.setId(tutorDoc.getId());
                        tutor.setName(tutorDoc.getString("name"));
                        tutor.setModules((List<String>) tutorDoc.get("modules"));
                        tutor.setProfileImageBase64(tutorDoc.getString("profileImageBase64"));
                        tutor.setStatus("Available");

                        tutorList.add(tutor);
                    }

                    // Hide the ProgressBar and show the CardStackView after data is loaded
                    progressBar.setVisibility(View.GONE);
                    cardStackView.setVisibility(View.VISIBLE);

                    if (tutorList.isEmpty()) {
                        Toast.makeText(this, "No tutors available at the moment.", Toast.LENGTH_SHORT).show();
                    }
                    cardStackTutorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Hide the ProgressBar and show an error message if fetching fails
                    progressBar.setVisibility(View.GONE);
                    cardStackView.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to fetch tutors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (tutorList.isEmpty()) return;

        Tutor swipedTutor = tutorList.get(cardStackLayoutManager.getTopPosition() - 1);

        if (direction == Direction.Left) {
            Toast.makeText(this, "Tutor rejected!", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onCardDragging(Direction direction, float ratio) {}
    @Override public void onCardRewound() {}
    @Override public void onCardCanceled() {}
    @Override public void onCardAppeared(View view, int position) {}
    @Override public void onCardDisappeared(View view, int position) {}
}