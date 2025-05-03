package com.example.dutpeertutoring;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_TIME = 2000;
    private FirebaseAuth mAuth;
    private TextView dateTimeText;
    private TextView appTitle;
    private CircularProgressIndicator progressIndicator;
    private ImageView logoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        MaterialCardView cardView = findViewById(R.id.cardView);
        dateTimeText = findViewById(R.id.dateTimeText);
        appTitle = findViewById(R.id.appTitle);
        progressIndicator = findViewById(R.id.progressIndicator);
        logoImage = findViewById(R.id.logoImage);
        BubbleView bubbleView = findViewById(R.id.bubbleView);

        // Set current date and time in UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = sdf.format(new Date());
        dateTimeText.setText(currentDateTime);

        // Set current user's login if available
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                String username = userEmail.split("@")[0];
                appTitle.setText(String.format("Peer 2 Peer, %s", username)); // Fix: Add username as the argument
            }
        } else {
            appTitle.setText("Peer 2 Peer"); // Default title if no user is logged in
        }

        // Apply animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        cardView.startAnimation(fadeIn);
        bubbleView.startAnimation(fadeIn);

        // Start delayed transition
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (mAuth.getCurrentUser() != null) {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            } else {
                intent = new Intent(MainActivity.this, RegisterActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }
}