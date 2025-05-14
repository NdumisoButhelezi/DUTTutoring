package com.example.dutpeertutoring;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TutorSessionActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private MaterialButton btnSelectPdf, btnUploadPdf, btnSubmitRating;
    private Uri selectedPdfUri;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TextView tutorNameTextView, tutorRatingStatus;
    private RatingBar ratingBar;
    private String tutorName, tutorSurname, tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_session);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnUploadPdf = findViewById(R.id.btnUploadPdf);
    ;

        // Get current tutor's ID
        tutorId = auth.getCurrentUser().getUid();

        // Fetch tutor's name and surname
        fetchTutorProfile();

        // Set listeners
        btnSelectPdf.setOnClickListener(view -> openFileChooser());
        btnUploadPdf.setOnClickListener(view -> {
            if (selectedPdfUri != null) {
                uploadPdfToFirestore(selectedPdfUri);
            } else {
                Toast.makeText(TutorSessionActivity.this, "Please select a PDF file first", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmitRating.setOnClickListener(view -> submitRating());
    }

    private void fetchTutorProfile() {
        firestore.collection("users").document(tutorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tutorName = documentSnapshot.getString("name");
                        tutorSurname = documentSnapshot.getString("surname");
                        String fullName = tutorName + " " + tutorSurname;
                        tutorNameTextView.setText(fullName); // Display tutor's name
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch tutor details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedPdfUri = data.getData();
            Toast.makeText(this, "PDF Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPdfToFirestore(Uri pdfUri) {
        try {
            // Code to upload the PDF file as before
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void submitRating() {
        float rating = ratingBar.getRating(); // Get the rating value

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("tutorId", tutorId);
        ratingData.put("tutorName", tutorName);
        ratingData.put("tutorSurname", tutorSurname);
        ratingData.put("rating", rating);
        ratingData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        firestore.collection("tutorRatings")
                .add(ratingData)
                .addOnSuccessListener(documentReference -> {
                    tutorRatingStatus.setText("Thank you for rating " + tutorName + "!");
                    tutorRatingStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                    Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}