package com.example.dutpeertutoring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TutorProfileSetupActivity extends AppCompatActivity {

    private static final int PICK_PROFILE_IMAGE = 1;
    private static final int PICK_ACADEMIC_RECORD = 2;

    private TextInputEditText nameInput, surnameInput;
    private ShapeableImageView profileImage;
    private ImageView academicRecordImage;
    private FloatingActionButton changePhotoButton, uploadAcademicRecordButton;
    private MultiAutoCompleteTextView modulesSelection;
    private MaterialButton saveProfileButton;
    private LinearProgressIndicator progressIndicator;
    private TextView academicRecordStatus;

    private Bitmap profileBitmap;
    private Bitmap academicRecordBitmap;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_profile_setup);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        surnameInput = findViewById(R.id.surnameInput);
        profileImage = findViewById(R.id.profileImage);
        academicRecordImage = findViewById(R.id.academicRecordImage);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        uploadAcademicRecordButton = findViewById(R.id.uploadAcademicRecordButton);
        modulesSelection = findViewById(R.id.modulesSelection);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        progressIndicator = findViewById(R.id.progressIndicator);
        academicRecordStatus = findViewById(R.id.academicRecordStatus);

        // Preloading modules
        String[] modules = getResources().getStringArray(R.array.modules_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, modules);
        modulesSelection.setAdapter(adapter);
        modulesSelection.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Set click listeners
        changePhotoButton.setOnClickListener(v -> pickImage(PICK_PROFILE_IMAGE));
        uploadAcademicRecordButton.setOnClickListener(v -> pickImage(PICK_ACADEMIC_RECORD));
        saveProfileButton.setOnClickListener(v -> saveTutorProfile());

        // Load existing profile if available
        loadTutorProfile();
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                switch (requestCode) {
                    case PICK_PROFILE_IMAGE:
                        profileBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        profileImage.setImageBitmap(profileBitmap);
                        break;
                    case PICK_ACADEMIC_RECORD:
                        academicRecordBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        academicRecordImage.setImageBitmap(academicRecordBitmap);
                        academicRecordStatus.setText("Academic record uploaded");
                        academicRecordStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveTutorProfile() {
        String name = nameInput.getText().toString().trim();
        String surname = surnameInput.getText().toString().trim();
        String modules = modulesSelection.getText().toString().trim();

        // Validate input
        if (name.isEmpty() || surname.isEmpty() || modules.isEmpty() || profileBitmap == null || academicRecordBitmap == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clean and validate modules
        modules = modules.replaceAll("\\s+", "");
        List<String> selectedModules = Arrays.asList(modules.split(","));

        // Show progress and disable save button
        progressIndicator.setVisibility(View.VISIBLE);
        saveProfileButton.setEnabled(false);

        // Convert profile image to Base64
        ByteArrayOutputStream profileBaos = new ByteArrayOutputStream();
        profileBitmap.compress(Bitmap.CompressFormat.JPEG, 70, profileBaos);
        String base64ProfileImage = Base64.encodeToString(profileBaos.toByteArray(), Base64.DEFAULT);

        // Convert academic record to Base64
        ByteArrayOutputStream academicBaos = new ByteArrayOutputStream();
        academicRecordBitmap.compress(Bitmap.CompressFormat.JPEG, 85, academicBaos);
        String base64AcademicRecord = Base64.encodeToString(academicBaos.toByteArray(), Base64.DEFAULT);

        // Create profile data map
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> tutorProfile = new HashMap<>();
        tutorProfile.put("name", name);
        tutorProfile.put("surname", surname);
        tutorProfile.put("modules", selectedModules);
        tutorProfile.put("profileImageBase64", base64ProfileImage);
        tutorProfile.put("academicRecordBase64", base64AcademicRecord);

        // Save to Firestore
        firestore.collection("users").document(userId)
                .set(tutorProfile)
                .addOnSuccessListener(aVoid -> {
                    progressIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                    saveProfileButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    saveProfileButton.setEnabled(true);
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTutorProfile() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String surname = documentSnapshot.getString("surname");
                        String base64ProfileImage = documentSnapshot.getString("profileImageBase64");
                        String base64AcademicRecord = documentSnapshot.getString("academicRecordBase64");
                        List<String> modules = (List<String>) documentSnapshot.get("modules");

                        // Populate fields
                        nameInput.setText(name);
                        surnameInput.setText(surname);
                        if (modules != null) {
                            modulesSelection.setText(String.join(", ", modules));
                        }

                        // Decode and display images
                        if (base64ProfileImage != null) {
                            byte[] decodedProfile = Base64.decode(base64ProfileImage, Base64.DEFAULT);
                            Bitmap profileBitmap = BitmapFactory.decodeByteArray(decodedProfile, 0, decodedProfile.length);
                            profileImage.setImageBitmap(profileBitmap);
                        }
                        if (base64AcademicRecord != null) {
                            byte[] decodedAcademic = Base64.decode(base64AcademicRecord, Base64.DEFAULT);
                            Bitmap academicBitmap = BitmapFactory.decodeByteArray(decodedAcademic, 0, decodedAcademic.length);
                            academicRecordImage.setImageBitmap(academicBitmap);
                            academicRecordStatus.setText("Academic record uploaded");
                            academicRecordStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}