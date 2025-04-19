package com.example.dutpeertutoring;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BookingPageActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TimePicker timePicker;
    private Spinner moduleSpinner;
    private Button bookButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private String tutorId, tutorName;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_page);

        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        moduleSpinner = findViewById(R.id.moduleSpinner);
        bookButton = findViewById(R.id.bookButton);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        studentId = auth.getCurrentUser().getUid(); // cussent use ID

        // Get tutor details from intent
        tutorId = getIntent().getStringExtra("tutorId");
        tutorName = getIntent().getStringExtra("tutorName");
        String[] tutorModules = getIntent().getStringArrayListExtra("tutorModules").toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tutorModules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moduleSpinner.setAdapter(adapter);

        bookButton.setOnClickListener(v -> bookSession());
    }

    private void bookSession() {
        String selectedModule = moduleSpinner.getSelectedItem().toString();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String dateTime = day + "/" + month + "/" + year + " " + hour + ":" + minute;

        Map<String, Object> booking = new HashMap<>();
        booking.put("studentId", studentId);
        booking.put("studentName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        booking.put("tutorId", tutorId);
        booking.put("tutorName", tutorName);
        booking.put("module", selectedModule);
        booking.put("dateTime", dateTime);
        booking.put("status", "Pending");

        firestore.collection("bookings").add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking request sent!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to book session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}