package com.example.dutpeertutoring;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TutorDetailActivity extends AppCompatActivity {

    private TextView nameTextView, surnameTextView, modulesTextView;
    private ImageView profileImageView, academicRecordImageView;
    private Button approveButton;
    private FirebaseFirestore firestore;
    private String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_detail);

        nameTextView = findViewById(R.id.nameTextView);
        surnameTextView = findViewById(R.id.surnameTextView);
        modulesTextView = findViewById(R.id.modulesTextView);
        profileImageView = findViewById(R.id.profileImageView);
        academicRecordImageView = findViewById(R.id.academicRecordImageView);
        approveButton = findViewById(R.id.approveButton);

        firestore = FirebaseFirestore.getInstance();
        tutorId = getIntent().getStringExtra("tutorId");

        loadTutorDetails();

        approveButton.setOnClickListener(v -> approveTutor());
    }

    private void loadTutorDetails() {
        firestore.collection("users").document(tutorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    String surname = documentSnapshot.getString("surname");
                    String modules = String.join(", ", (List<String>) documentSnapshot.get("modules"));
                    String profileImageBase64 = documentSnapshot.getString("profileImageBase64");
                    String academicRecordBase64 = documentSnapshot.getString("academicRecordBase64");

                    nameTextView.setText("Name: " + name);
                    surnameTextView.setText("Surname: " + surname);
                    modulesTextView.setText("Modules: " + modules);

                    // Decode and display images
                    if (profileImageBase64 != null) {
                        byte[] profileBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                        Bitmap profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);
                        profileImageView.setImageBitmap(profileBitmap);
                    }

                    if (academicRecordBase64 != null) {
                        byte[] academicBytes = Base64.decode(academicRecordBase64, Base64.DEFAULT);
                        Bitmap academicBitmap = BitmapFactory.decodeByteArray(academicBytes, 0, academicBytes.length);
                        academicRecordImageView.setImageBitmap(academicBitmap);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load tutor details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void approveTutor() {
        firestore.collection("users").document(tutorId)
                .update("isConfirmed", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tutor approved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to approve tutor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}