package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SupportActivity extends AppCompatActivity {

    private EditText issueInput;
    private Button submitButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        issueInput = findViewById(R.id.issueInput);
        submitButton = findViewById(R.id.submitButton);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        submitButton.setOnClickListener(v -> submitIssue());
    }

    private void submitIssue() {
        String issueText = issueInput.getText().toString().trim();
        String userId = auth.getCurrentUser().getUid();

        if (issueText.isEmpty()) {
            Toast.makeText(this, "Please describe the issue", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> issue = new HashMap<>();
        issue.put("userId", userId);
        issue.put("issue", issueText);
        issue.put("timestamp", System.currentTimeMillis());

        firestore.collection("supportTickets").add(issue)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Issue submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit issue: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}