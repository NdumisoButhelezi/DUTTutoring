package com.example.dutpeertutoring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TutorProfileSetupActivity extends AppCompatActivity {

    private static final int PICK_PROFILE_IMAGE = 1;
    private static final int PICK_ACADEMIC_RECORD_IMAGE = 2;

    private EditText nameInput, surnameInput;
    private ImageView profileImage, academicRecordImage;
    private MultiAutoCompleteTextView modulesSelection;
    private Button saveProfileButton;

    private Uri profileImageUri, academicRecordUri;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_profile_setup);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.nameInput);
        surnameInput = findViewById(R.id.surnameInput);
        profileImage = findViewById(R.id.profileImage);
        academicRecordImage = findViewById(R.id.academicRecordImage);
        modulesSelection = findViewById(R.id.modulesSelection);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        // Preloading modules
        String[] modules = getResources().getStringArray(R.array.modules_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, modules);
        modulesSelection.setAdapter(adapter);
        modulesSelection.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        profileImage.setOnClickListener(v -> pickImage(PICK_PROFILE_IMAGE));
        academicRecordImage.setOnClickListener(v -> pickImage(PICK_ACADEMIC_RECORD_IMAGE));

        saveProfileButton.setOnClickListener(v -> saveTutorProfile());
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                if (requestCode == PICK_PROFILE_IMAGE) {
                    profileImage.setImageBitmap(bitmap);
                    profileImageUri = imageUri;
                } else if (requestCode == PICK_ACADEMIC_RECORD_IMAGE) {
                    academicRecordImage.setImageBitmap(bitmap);
                    academicRecordUri = imageUri;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTutorProfile() {
        String name = nameInput.getText().toString().trim();
        String surname = surnameInput.getText().toString().trim();
        String modules = modulesSelection.getText().toString().trim();

        if (name.isEmpty() || surname.isEmpty() || profileImageUri == null || academicRecordUri == null || modules.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedModules = Arrays.asList(modules.split(","));
        if (selectedModules.size() > 4) {
            Toast.makeText(this, "You can select up to 4 modules only.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> tutorProfile = new HashMap<>();
        tutorProfile.put("name", name);
        tutorProfile.put("surname", surname);
        tutorProfile.put("modules", selectedModules);
        tutorProfile.put("profileComplete", true);

        // Save profile to Firestore
        firestore.collection("users").document(userId).update(tutorProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    // Redirect to tutor dashboard
                    startActivity(new Intent(TutorProfileSetupActivity.this, TutorDashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}