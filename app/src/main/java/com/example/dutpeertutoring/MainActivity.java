package com.example.dutpeertutoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private static final int STORAGE_PERMISSION_CODE = 1002;
    private FirebaseAuth mAuth;
    private TextView dateTimeText;
    private TextView appTitle;
    private CircularProgressIndicator progressIndicator;
    private ImageView logoImage;

    // ActivityResultLauncher for requesting permissions
    private ActivityResultLauncher<String[]> permissionLauncher;

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

        // Setup permission launcher for handling permission requests
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false) && result.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
                // Permission granted
                Toast.makeText(MainActivity.this, "Storage permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(MainActivity.this, "Storage permissions denied. Cannot upload/download PDFs.", Toast.LENGTH_SHORT).show();
            }
        });

        // Start delayed transition with permission request
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Request permissions after the splash screen animation finishes
            requestStoragePermission();

            // Transition to the next activity after checking permissions
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

    // Check for storage permission
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Request storage permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request MANAGE_EXTERNAL_STORAGE permission for Android 13+
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                try {
                    startActivityForResult(intent, STORAGE_PERMISSION_CODE);
                } catch (Exception e) {
                    Toast.makeText(this, "Permission request not available on this device.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    // Handle storage permission result (this is now handled by ActivityResultLauncher)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file interaction
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied. Cannot upload or download PDFs.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }
}
