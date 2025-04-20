package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleSuggestionsActivity extends AppCompatActivity {

    private ListView suggestionsListView;
    private ModuleSuggestionAdapter adapter;
    private List<ModuleSuggestion> suggestionsList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_suggestions);

        // Initialize UI components
        suggestionsListView = findViewById(R.id.suggestionsListView);
        suggestionsList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up adapter
        adapter = new ModuleSuggestionAdapter(
                this,
                suggestionsList,
                suggestion -> handleVote(suggestion, true),  // Upvote action
                suggestion -> handleVote(suggestion, false) // Downvote action
        );
        suggestionsListView.setAdapter(adapter);

        // Fetch suggestions and listen for updates
        listenForSuggestions();
    }

    private void listenForSuggestions() {
        // Listen for real-time updates from Firestore
        firestore.collection("moduleSuggestions")
                .whereEqualTo("status", "Pending") // Fetch only pending suggestions
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(ModuleSuggestionsActivity.this, "Failed to fetch suggestions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            for (DocumentChange change : value.getDocumentChanges()) {
                                switch (change.getType()) {
                                    case ADDED:
                                        // Add new suggestion
                                        ModuleSuggestion newSuggestion = change.getDocument().toObject(ModuleSuggestion.class);
                                        newSuggestion.setId(change.getDocument().getId());
                                        suggestionsList.add(newSuggestion);
                                        break;

                                    case MODIFIED:
                                        // Update existing suggestion
                                        String updatedId = change.getDocument().getId();
                                        for (int i = 0; i < suggestionsList.size(); i++) {
                                            if (suggestionsList.get(i).getId().equals(updatedId)) {
                                                ModuleSuggestion updatedSuggestion = change.getDocument().toObject(ModuleSuggestion.class);
                                                updatedSuggestion.setId(updatedId);
                                                suggestionsList.set(i, updatedSuggestion);
                                                break;
                                            }
                                        }
                                        break;

                                    case REMOVED:
                                        // Remove deleted suggestion
                                        String removedId = change.getDocument().getId();
                                        suggestionsList.removeIf(suggestion -> suggestion.getId().equals(removedId));
                                        break;
                                }
                            }

                            // Notify adapter about data changes
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void handleVote(ModuleSuggestion suggestion, boolean isUpvote) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get existing votes or initialize a new one
        Map<String, Boolean> votes = suggestion.getVotes() != null ? suggestion.getVotes() : new HashMap<>();

        // Prevent duplicate voting
        if (votes.containsKey(userId)) {
            Toast.makeText(this, "You have already voted for this suggestion!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add the user's vote
        votes.put(userId, isUpvote);

        // Update Firestore with the new votes
        firestore.collection("moduleSuggestions")
                .document(suggestion.getId())
                .update("votes", votes)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Vote submitted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit vote: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}