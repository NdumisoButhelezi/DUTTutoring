package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Spinner roleSpinner;
    private Button registerButton;
    private ProgressBar progressBar;
    private TextView loginLink;
    private CardView registerCard;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        roleSpinner = findViewById(R.id.roleSpinner);
        registerButton = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBar);
        loginLink = findViewById(R.id.loginLink);
        registerCard = findViewById(R.id.registerCard);

        // Apply entrance animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        registerCard.startAnimation(fadeIn);

        // Set up the roles dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Set click listeners
        registerButton.setOnClickListener(v -> {
            // Apply button click animation
            Animation buttonAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            registerButton.startAnimation(buttonAnim);
            registerUser();
        });

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        // Input validation
        if (TextUtils.isEmpty(email)) {
            showError(emailInput, "Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError(passwordInput, "Password is required");
            return;
        }

        if (password.length() < 6) {
            showError(passwordInput, "Password must be at least 6 characters");
            return;
        }

        if (role.equals("Select Role")) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                // Save the user's role in Firestore
                Map<String, Object> user = new HashMap<>();
                user.put("email", email);
                user.put("role", role);
                user.put("isConfirmed", false); // Default to unconfirmed
                user.put("profileComplete", false); // Default to incomplete

                firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);

                            // Show success animation and message
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                            // Redirect based on role
                            Intent intent;
                            switch (role) {
                                case "Tutor":
                                    intent = new Intent(RegisterActivity.this, TutorProfileSetupActivity.class);
                                    break;
                                case "Admin":
                                    intent = new Intent(RegisterActivity.this, AdminDashboardActivity.class);
                                    break;
                                case "Student/Tutee":
                                    intent = new Intent(RegisterActivity.this, StudentDashboardActivity.class);
                                    break;
                                default:
                                    intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    break;
                            }

                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(EditText input, String message) {
        input.setError(message);
        input.requestFocus();
        // Shake animation for error
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        input.startAnimation(shake);
    }
}