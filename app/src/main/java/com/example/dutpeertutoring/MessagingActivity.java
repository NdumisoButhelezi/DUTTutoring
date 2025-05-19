package com.example.dutpeertutoring;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

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
import com.google.firebase.database.Query;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private Spinner bookingSpinner;
    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private LinearProgressIndicator loadingProgressBar;
    private MessageRecyclerAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseFirestore firestore;
    private DatabaseReference messagesRef;    // <— this must be set before sending

    private List<Booking> paidBookings;
    private String currentUserId;
    private String selectedBookingId;
    private String selectedTutorId;

    private boolean firstBookingSelection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        bookingSpinner       = findViewById(R.id.bookingSpinner);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput         = findViewById(R.id.messageInput);
        sendButton           = findViewById(R.id.sendButton);
        loadingProgressBar   = findViewById(R.id.loadingProgressBar);

        // disable until chat is ready
        messageInput.setEnabled(false);
        sendButton.setEnabled(false);

        // RecyclerView setup
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList    = new ArrayList<>();
        currentUserId  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageAdapter = new MessageRecyclerAdapter(messageList, currentUserId);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Firestore & booking list
        firestore    = FirebaseFirestore.getInstance();
        paidBookings = new ArrayList<>();
        fetchPaidBookings();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void fetchPaidBookings() {
        loadingProgressBar.setVisibility(View.VISIBLE);

        firestore.collection("bookings")
                .whereEqualTo("studentId", currentUserId)
                .whereEqualTo("status", "Approved:Paid")
                .get()
                .addOnSuccessListener(snaps -> {
                    paidBookings.clear();
                    List<String> titles = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snaps) {
                        Booking b = doc.toObject(Booking.class);
                        b.setId(doc.getId());
                        paidBookings.add(b);
                        titles.add(b.getModule() + " (" + b.getDate() + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, titles
                    );
                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );
                    bookingSpinner.setAdapter(adapter);

                    bookingSpinner.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(
                                        AdapterView<?> parent, View view, int position, long id
                                ) {
                                    if (firstBookingSelection) {
                                        firstBookingSelection = false;
                                        return;
                                    }
                                    Booking sel = paidBookings.get(position);
                                    selectedBookingId = sel.getId();
                                    selectedTutorId  = sel.getTutorId();
                                    loadMessages();
                                }
                                @Override public void onNothingSelected(AdapterView<?> parent) {}
                            }
                    );

                    loadingProgressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(
                            this,
                            "Failed to load bookings.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void loadMessages() {
        if (selectedBookingId == null) return;

        loadingProgressBar.setVisibility(View.VISIBLE);

        // 1) Build the reference for this booking's chat
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("messages")
                .child(selectedBookingId);

        // 2) Save it to your field so sendMessage() can use it
        messagesRef = ref;

        // 3) (Optional) keep it synced & only fetch last 10
        ref.keepSynced(true);
        Query recent = ref.orderByChild("timestamp").limitToLast(10);

        // 4) Clear old UI
        messageList.clear();
        messageAdapter.notifyDataSetChanged();

        // 5) Stream in each message
        recent.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snap, String prevKey) {
                Message m = snap.getValue(Message.class);
                messageList.add(m);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                messagesRecyclerView.scrollToPosition(messageList.size() - 1);

                loadingProgressBar.setVisibility(View.GONE);
                messageInput.setEnabled(true);
                sendButton.setEnabled(true);
            }
            @Override public void onChildChanged(DataSnapshot s, String k) {}
            @Override public void onChildRemoved(DataSnapshot s) {}
            @Override public void onChildMoved(DataSnapshot s, String k) {}
            @Override
            public void onCancelled(DatabaseError error) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(
                        MessagingActivity.this,
                        "Failed to load messages.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();

        // guard: no chat selected
        if (messagesRef == null) {
            Toast.makeText(
                    this,
                    "Please select a booking first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (text.isEmpty()) {
            Toast.makeText(
                    this,
                    "Type a message before sending",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // disable until write completes
        sendButton.setEnabled(false);
        loadingProgressBar.setVisibility(View.VISIBLE);

        String msgId = messagesRef.push().getKey();
        Message msg = new Message(
                msgId,
                currentUserId,
                selectedTutorId,
                text,
                System.currentTimeMillis()
        );
        messagesRef.child(msgId)
                .setValue(msg)
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Failed to send",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnCompleteListener(task -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                });
    }
}
