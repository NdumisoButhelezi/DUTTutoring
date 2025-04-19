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

public class ModuleSuggestionActivity extends AppCompatActivity {

    private EditText moduleInput;
    private Button submitButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_suggestion);

        moduleInput = findViewById(R.id.moduleInput);
        submitButton = findViewById(R.id.submitButton);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        submitButton.setOnClickListener(v -> submitSuggestion());
    }

    private void submitSuggestion() {
        String moduleName = moduleInput.getText().toString().trim();
        String studentId = auth.getCurrentUser().getUid();

        if (moduleName.isEmpty()) {
            Toast.makeText(this, "Please enter a module name", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put("moduleName", moduleName);
        suggestion.put("studentId", studentId);
        suggestion.put("timestamp", System.currentTimeMillis());

        firestore.collection("moduleSuggestions").add(suggestion)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Suggestion submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit suggestion: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}