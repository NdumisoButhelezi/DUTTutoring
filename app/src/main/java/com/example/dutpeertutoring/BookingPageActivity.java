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
    private TimePicker startTimePicker, endTimePicker;
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

        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);
        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);

        moduleSpinner = findViewById(R.id.moduleSpinner);
        bookButton = findViewById(R.id.bookButton);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        studentId = auth.getCurrentUser().getUid();

        tutorId = getIntent().getStringExtra("tutorId");
        tutorName = getIntent().getStringExtra("tutorName");
        String[] tutorModules = getIntent().getStringArrayListExtra("tutorModules").toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tutorModules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moduleSpinner.setAdapter(adapter);

        bookButton.setOnClickListener(v -> validateAndBookSession());
    }

    private void validateAndBookSession() {
        String selectedModule = moduleSpinner.getSelectedItem().toString();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();

        int startHour = startTimePicker.getHour();
        int startMinute = startTimePicker.getMinute();
        int endHour = endTimePicker.getHour();
        int endMinute = endTimePicker.getMinute();

        String date = String.format("%04d-%02d-%02d", year, month, day);
        String startTime = String.format("%02d:%02d", startHour, startMinute);
        String endTime = String.format("%02d:%02d", endHour, endMinute);

        if (selectedModule.isEmpty()) {
            Toast.makeText(this, "Please select a module.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
            Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("studentId", studentId);
        booking.put("tutorId", tutorId);
        booking.put("tutorName", tutorName);
        booking.put("module", selectedModule);
        booking.put("date", date);
        booking.put("startTime", startTime);
        booking.put("endTime", endTime);
        booking.put("status", "Pending");
        booking.put("paid", false);

        firestore.collection("bookings").whereEqualTo("studentId", studentId)
                .whereEqualTo("tutorId", tutorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "You already have a pending booking with this tutor!", Toast.LENGTH_SHORT).show();
                    } else {
                        firestore.collection("bookings").add(booking)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Booking request sent!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to book session: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }
}