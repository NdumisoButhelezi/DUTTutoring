package com.example.dutpeertutoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TutorSessionActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private MaterialButton btnSelectPdf, btnUploadPdf;
    private Uri selectedPdfUri;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TextView tutorNameTextView, tutorRatingStatus;
    private RatingBar ratingBar;
    private String tutorName, tutorSurname, tutorId;
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_session);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages"); // Initialize messagesRef to point to "messages" node

        // Initialize UI components
        tutorNameTextView = findViewById(R.id.tutorNameTextView);
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnUploadPdf = findViewById(R.id.btnUploadPdf);

        // Fetch tutor's name and surname
        fetchTutorProfile();

        // Set listeners for buttons
        btnSelectPdf.setOnClickListener(view -> openFileChooser());
        btnUploadPdf.setOnClickListener(view -> {
            if (selectedPdfUri != null) {
                try {
                    uploadPdfToFirestore(selectedPdfUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(TutorSessionActivity.this, "Error reading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TutorSessionActivity.this, "Please select a PDF file first", Toast.LENGTH_SHORT).show();
            }
        });

        // Chat Button
        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(view -> {
            Intent intent = new Intent(TutorSessionActivity.this, MessagingActivity.class);
            intent.putExtra("isTutor", true);  // Pass flag to MessagingActivity indicating this is a tutor
            startActivity(intent);
        });

        // Request storage permissions if not granted
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }
    }

    private void fetchTutorProfile() {
        firestore.collection("users").document(tutorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tutorName = documentSnapshot.getString("name");
                        tutorSurname = documentSnapshot.getString("surname");
                        String fullName = tutorName + " " + tutorSurname;
                        if (tutorNameTextView != null) {
                            tutorNameTextView.setText(fullName); // Display tutor's name
                        }
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

    private void uploadPdfToFirestore(Uri pdfUri) throws IOException {
        // Read the file and convert it to Base64
        byte[] pdfBytes = readPdfFromUri(pdfUri);
        String encodedPdf = Base64.encodeToString(pdfBytes, Base64.DEFAULT);

        // Upload PDF encoded data to Firestore
        Map<String, Object> pdfData = new HashMap<>();
        pdfData.put("pdfData", encodedPdf);
        pdfData.put("tutorId", tutorId);
        pdfData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        firestore.collection("resources")
                .add(pdfData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "PDF uploaded successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private byte[] readPdfFromUri(Uri pdfUri) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (FileInputStream fileInputStream = new FileInputStream(pdfUri.getPath())) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        }
        return byteArrayOutputStream.toByteArray();
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

    // Check for storage permission
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Request storage permission
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    // Handle storage permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot upload or download PDFs.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
