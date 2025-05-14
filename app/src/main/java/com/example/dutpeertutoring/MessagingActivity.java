package com.example.dutpeertutoring;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private Spinner bookingSpinner; // Dropdown for selecting bookings
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private MessageRecyclerAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseFirestore firestore;
    private DatabaseReference messagesRef;

    private List<Booking> paidBookings;
    private String currentUserId;
    private String selectedBookingId;
    private String selectedTutorId;
    private String selectedStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Initialize views
        bookingSpinner = findViewById(R.id.bookingSpinner);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messageList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        paidBookings = new ArrayList<>();

        messageAdapter = new MessageRecyclerAdapter(messageList, currentUserId);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Fetch paid bookings
        fetchPaidBookings();

        // Send button click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void fetchPaidBookings() {
        firestore.collection("bookings")
                .whereEqualTo("studentId", currentUserId)
                .whereEqualTo("status", "Approved:Paid")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    paidBookings.clear();
                    List<String> bookingTitles = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());
                        paidBookings.add(booking);
                        bookingTitles.add(booking.getModule() + " (" + booking.getDate() + ")");
                    }

                    // Populate the spinner with booking titles
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bookingTitles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    bookingSpinner.setAdapter(adapter);

                    // Set up listener for booking selection
                    bookingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Booking selectedBooking = paidBookings.get(position);
                            selectedBookingId = selectedBooking.getId();
                            selectedTutorId = selectedBooking.getTutorId();
                            selectedStudentId = selectedBooking.getStudentId();
                            loadMessages();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Do nothing
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load bookings.", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        if (selectedBookingId == null) return;

        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(selectedBookingId);
        messagesRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the bottom
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(MessagingActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty() || selectedBookingId == null) {
            Toast.makeText(this, "Please select a booking and enter a message.", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = messagesRef.push().getKey();
        Message message = new Message(
                messageId, currentUserId, selectedTutorId, messageText, System.currentTimeMillis()
        );
        messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                    Toast.makeText(MessagingActivity.this, "Message sent.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(MessagingActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show());
    }
}