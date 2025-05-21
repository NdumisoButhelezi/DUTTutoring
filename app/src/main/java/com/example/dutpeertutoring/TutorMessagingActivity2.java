package com.example.dutpeertutoring;

import android.os.Bundle;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TutorMessagingActivity2 extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private LinearProgressIndicator loadingProgressBar;
    private Spinner bookingSpinner;

    private MessageRecyclerAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();

    private FirebaseFirestore firestore;

    private String currentUserId;
    private String selectedBookingId;
    private String selectedReceiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Initialize views
        bookingSpinner = findViewById(R.id.bookingSpinner);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Setup RecyclerView for displaying messages
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageAdapter = new MessageRecyclerAdapter(messageList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Fetch bookings for tutor
        fetchBookingsForTutor();

        // Setup send button listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    // Fetch bookings for tutors
    private void fetchBookingsForTutor() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        firestore.collection("bookings")
                .whereEqualTo("tutorId", currentUserId)
                .whereEqualTo("status", "Approved:Paid")
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Booking> results = new ArrayList<>();
                    for (var doc : snaps) {
                        Booking b = doc.toObject(Booking.class);
                        b.setId(doc.getId());
                        results.add(b);
                    }
                    bindBookingSpinner(results);
                    loadingProgressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not load your tutoring sessions", Toast.LENGTH_SHORT).show();
                });
    }

    // Bind the bookings to the spinner
    private void bindBookingSpinner(List<Booking> results) {
        bookings.clear();
        bookings.addAll(results);

        List<String> titles = new ArrayList<>();
        for (Booking b : bookings) {
            titles.add(b.getModule() + " (" + b.getDate() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, titles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookingSpinner.setAdapter(adapter);

        bookingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstSelection) {
                    firstSelection = false;
                    return;
                }
                Booking selected = bookings.get(position);
                selectedBookingId = selected.getId();
                selectedReceiverId = selected.getStudentId();  // Get the studentId to send messages to
                loadMessagesForBooking();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Load messages for the selected booking
    private void loadMessagesForBooking() {
        if (selectedBookingId == null) return;

        loadingProgressBar.setVisibility(View.VISIBLE);
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("messages")
                .child(selectedBookingId);  // Firebase Realtime DB path

        messagesRef.orderByChild("timestamp").limitToLast(50)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null && message.getReceiverId().equals(currentUserId)) {
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                        }
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(TutorMessagingActivity2.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Send a message
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Type a message before sending", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("messages")
                .child(selectedBookingId);  // Firebase Realtime DB path

        String msgId = messagesRef.push().getKey(); // Firebase automatically generates a unique message ID

        // Create the message object
        Message msg = new Message(
                msgId,
                currentUserId,            // Sender is the current user (tutor)
                selectedReceiverId,       // Receiver is the student
                text,
                System.currentTimeMillis()  // Timestamp for the message
        );

        // Write the message to Firebase Realtime Database
        messagesRef.child(msgId)
                .setValue(msg)
                .addOnCompleteListener(task -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        messageInput.setText("");  // Clear the input field after sending
                    } else {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
