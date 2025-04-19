package com.example.dutpeertutoring;

;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, registerButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginBtn);
        registerButton = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                firestore.collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String role = documentSnapshot.getString("role");
                            Boolean profileComplete = documentSnapshot.getBoolean("profileComplete");

                            if (role != null) {
                                if ("Security".equals(role)) { // Assuming "Security" is the role for tutors
                                    if (Boolean.TRUE.equals(profileComplete)) {
                                        startActivity(new Intent(LoginActivity.this, TutorDashboardActivity.class));
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, TutorProfileSetupActivity.class));
                                    }
                                } else if ("Admin".equals(role)) {
                                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                } else if ("Student/Staff".equals(role)) {
                                    startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
                                } else {
                                    Toast.makeText(LoginActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                }
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Failed to fetch role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}