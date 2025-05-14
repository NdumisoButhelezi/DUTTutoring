package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private CardView loginCard;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ImageButton passwordToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);
        progressBar = findViewById(R.id.progressBar);
        loginCard = findViewById(R.id.loginCard);
        passwordToggle = findViewById(R.id.passwordToggle);

        // Toggle password visibility
        passwordToggle.setOnClickListener(v -> {
            if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_visibility);
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_visibility_off);
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });

        // Apply entrance animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        loginCard.startAnimation(fadeIn);

        // Set click listeners
        loginButton.setOnClickListener(v -> {
            // Apply button click animation
            Animation buttonAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            loginButton.startAnimation(buttonAnim);
            loginUser();
        });

        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(email)) {
            showError(emailInput, "Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError(passwordInput, "Password is required");
            return;
        }

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                firestore.collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            progressBar.setVisibility(View.GONE);

                            String role = documentSnapshot.getString("role");
                            Boolean profileComplete = documentSnapshot.getBoolean("profileComplete");
                            Boolean approved = documentSnapshot.getBoolean("approved");

                            if (role != null) {
                                Intent intent;
                                switch (role) {
                                    case "Tutor":
                                        if (Boolean.TRUE.equals(approved)) {
                                            if (Boolean.TRUE.equals(profileComplete)) {
                                                intent = new Intent(LoginActivity.this, TutorDashboardActivity.class);
                                            } else {
                                                intent = new Intent(LoginActivity.this, TutorDashboardActivity.class);
                                            }
                                            startActivity(intent);
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                            finish();
                                        } else {
                                            loginButton.setEnabled(true);
                                            Toast.makeText(this, "Your account has not been approved yet.", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case "Admin":
                                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        finish();
                                        break;
                                    case "Student/Tutee":
                                        intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        finish();
                                        break;
                                    default:
                                        loginButton.setEnabled(true);
                                        Toast.makeText(LoginActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            } else {
                                loginButton.setEnabled(true);
                                Toast.makeText(LoginActivity.this, "Role not defined for this user.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Failed to fetch role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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