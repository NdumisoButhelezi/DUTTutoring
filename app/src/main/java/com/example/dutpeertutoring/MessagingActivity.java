package com.example.dutpeertutoring;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private LinearProgressIndicator loadingProgressBar;
    private MessageRecyclerAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();
    private String currentUserId;
    private String selectedBookingId;
    private String selectedReceiverId;
    private FirebaseFirestore firestore;
    private DatabaseReference messagesRef;

    // Spinner for both Tutors and Students
    private Spinner bookingSpinner;
    private ArrayAdapter<String> bookingAdapter;
    private final List<BookingInfo> bookingList = new ArrayList<>();
    private final List<String> bookingDisplayList = new ArrayList<>();

    // Role
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        bookingSpinner = findViewById(R.id.bookingSpinner);

        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get current user role from Firestore
        firestore.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                currentUserRole = userDoc.getString("role");
                if ("Tutor".equals(currentUserRole)) {
                    setupBookingSpinnerForTutors();
                } else if ("Student/Tutee".equals(currentUserRole)) {
                    setupBookingSpinnerForStudents();
                } else {
                    Toast.makeText(this, "Unrecognized role. Contact support.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        messageAdapter = new MessageRecyclerAdapter(messageList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Send button listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupBookingSpinnerForTutors() {
        // Query bookings where currentUserId == tutorId AND paid == true
        firestore.collection("bookings")
                .whereEqualTo("tutorId", currentUserId)
                .whereEqualTo("paid", true)
                .get()
                .addOnSuccessListener(querySnapshots -> populateBookingSpinner(querySnapshots, "studentId", "module", "date"))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupBookingSpinnerForStudents() {
        // Query bookings where currentUserId == studentId AND paid == true
        firestore.collection("bookings")
                .whereEqualTo("studentId", currentUserId)
                .whereEqualTo("paid", true)
                .get()
                .addOnSuccessListener(querySnapshots -> populateBookingSpinner(querySnapshots, "tutorId", "module", "date"))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateBookingSpinner(QuerySnapshot querySnapshots, String otherPartyIdField, String subjectField, String dateField) {
        bookingList.clear();
        bookingDisplayList.clear();
        for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
            final String bookingId = doc.getId();
            final String otherPartyId = doc.getString(otherPartyIdField);
            final String subject = doc.getString(subjectField);
            final String date = doc.getString(dateField);
            if (otherPartyId == null || date == null) continue;

            // Fetch name/email of the other party for display
            firestore.collection("users").document(otherPartyId).get()
                    .addOnSuccessListener(otherPartyDoc -> {
                        String otherPartyName = "User";
                        if (otherPartyDoc.exists() && otherPartyDoc.getString("email") != null) {
                            otherPartyName = otherPartyDoc.getString("email");
                        }

                        // Add booking info for Spinner
                        BookingInfo info = new BookingInfo(bookingId, otherPartyId, otherPartyName, subject, date);
                        bookingList.add(info);
                        bookingDisplayList.add(otherPartyName + " (" + (subject != null ? subject : "No Subject") + ") - " + date);

                        // After all bookings are loaded, update the spinner
                        if (bookingList.size() == bookingDisplayList.size()) {
                            updateBookingSpinner();
                        }
                    });
        }

        if (bookingList.isEmpty()) {
            Toast.makeText(this, "No paid bookings found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookingSpinner() {
        bookingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bookingDisplayList);
        bookingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookingSpinner.setAdapter(bookingAdapter);
        bookingSpinner.setVisibility(View.VISIBLE);

        bookingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BookingInfo selectedBooking = bookingList.get(position);
                selectedBookingId = selectedBooking.bookingId;
                selectedReceiverId = selectedBooking.otherPartyId;
                messageList.clear();
                messageAdapter.notifyDataSetChanged();
                loadMessagesForBooking();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Optionally, auto-select the first booking if available
        if (!bookingList.isEmpty()) {
            bookingSpinner.setSelection(0);
        }
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Type a message before sending", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBookingId == null || selectedReceiverId == null) {
            Toast.makeText(this, "No session or receiver found. Unable to send message.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (messagesRef == null) {
            messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(selectedBookingId);
        }

        // Create the message object
        String msgId = messagesRef.push().getKey();

        Message msg = new Message(
                msgId,
                currentUserId,
                selectedReceiverId,
                text,
                System.currentTimeMillis()
        );

        // Write the message to Firebase Realtime Database
        messagesRef.child(msgId)
                .setValue(msg)
                .addOnCompleteListener(task -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                    } else {
                        Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessagesForBooking() {
        if (selectedBookingId == null) return;

        loadingProgressBar.setVisibility(View.VISIBLE);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(selectedBookingId);

        messageList.clear();
        messageAdapter.notifyDataSetChanged();

        messagesRef.orderByChild("timestamp").limitToLast(50)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                        Message m = snapshot.getValue(Message.class);
                        if (m != null) {
                            messageList.add(m);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                        }
                        loadingProgressBar.setVisibility(View.GONE);
                        messageInput.setEnabled(true);
                        sendButton.setEnabled(true);
                    }

                    @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(MessagingActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper class for bookings
    private static class BookingInfo {
        String bookingId;
        String otherPartyId;
        String otherPartyName;
        String subject;
        String date;

        BookingInfo(String bookingId, String otherPartyId, String otherPartyName, String subject, String date) {
            this.bookingId = bookingId;
            this.otherPartyId = otherPartyId;
            this.otherPartyName = otherPartyName;
            this.subject = subject;
            this.date = date;
        }
    }
}