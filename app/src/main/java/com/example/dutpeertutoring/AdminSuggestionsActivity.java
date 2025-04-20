package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminSuggestionsActivity extends AppCompatActivity {

    private ListView adminListView;
    private ModuleSuggestionAdapter adapter;
    private List<ModuleSuggestion> suggestionsList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_suggestions);

        adminListView = findViewById(R.id.adminListView);
        suggestionsList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        adapter = new ModuleSuggestionAdapter(this, suggestionsList, this::approveSuggestion, this::rejectSuggestion);
        adminListView.setAdapter(adapter);

        fetchSuggestions();
    }

    private void fetchSuggestions() {
        firestore.collection("moduleSuggestions")
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    suggestionsList.clear();
                    for (var document : queryDocumentSnapshots) {
                        ModuleSuggestion suggestion = document.toObject(ModuleSuggestion.class);
                        suggestion.setId(document.getId());
                        suggestionsList.add(suggestion);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch suggestions: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void approveSuggestion(ModuleSuggestion suggestion) {
        updateSuggestionStatus(suggestion, "Approved");
    }

    private void rejectSuggestion(ModuleSuggestion suggestion) {
        updateSuggestionStatus(suggestion, "Rejected");
    }

    private void updateSuggestionStatus(ModuleSuggestion suggestion, String status) {
        firestore.collection("moduleSuggestions").document(suggestion.getId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Suggestion " + status.toLowerCase() + "!", Toast.LENGTH_SHORT).show();
                    fetchSuggestions();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update suggestion: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}