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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
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

        datePicker = findViewById(R.id.datePicker);
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

        // Validate module selection
        if (selectedModule.isEmpty()) {
            Toast.makeText(this, "Please select a module.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract date from the date picker
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();

        // Use Calendar to compare booking date with the current date plus two days
        Calendar current = Calendar.getInstance();
        Calendar minBookingDate = Calendar.getInstance();
        minBookingDate.add(Calendar.DAY_OF_YEAR, 2);

        Calendar bookingDate = Calendar.getInstance();
        // Note: month - 1 is required because Calendar months are zero-indexed.
        bookingDate.set(year, month - 1, day, 0, 0, 0);
        bookingDate.set(Calendar.MILLISECOND, 0);

        if (bookingDate.before(minBookingDate)) {
            Toast.makeText(this, "Bookings must be made at least 2 days in advance.", Toast.LENGTH_SHORT).show();
            return;
        }

        int startHour = startTimePicker.getHour();
        int startMinute = startTimePicker.getMinute();
        int endHour = endTimePicker.getHour();
        int endMinute = endTimePicker.getMinute();

        String startTime = String.format("%02d:%02d", startHour, startMinute);
        String endTime = String.format("%02d:%02d", endHour, endMinute);

        // Validate that end time is after start time.
        if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
            Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure booking duration is exactly 1 hour (60 minutes)
        int newStartMinutes = convertToMinutes(startTime);
        int newEndMinutes = convertToMinutes(endTime);
        int duration = newEndMinutes - newStartMinutes;
        if (duration != 60) {
            Toast.makeText(this, "Booking duration must be exactly one hour.", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = String.format("%04d-%02d-%02d", year, month, day);

        // First, verify if there's any booking overlap for the same tutor on the chosen date.
        firestore.collection("bookings")
                .whereEqualTo("tutorId", tutorId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean conflict = false;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String existingStart = doc.getString("startTime");
                        String existingEnd = doc.getString("endTime");
                        int existingStartMinutes = convertToMinutes(existingStart);
                        int existingEndMinutes = convertToMinutes(existingEnd);
                        // Check for overlap: the new booking overlaps if its start is before an existing booking's end,
                        // and its end is after an existing booking's start.
                        if (newStartMinutes < existingEndMinutes && newEndMinutes > existingStartMinutes) {
                            conflict = true;
                            break;
                        }
                    }

                    if (conflict) {
                        Toast.makeText(BookingPageActivity.this, "The tutor is already booked for the selected time.", Toast.LENGTH_SHORT).show();
                    } else {
                        // If no conflict, continue to add the booking.
                        createBooking(date, startTime, endTime, selectedModule);
                    }
                });
    }

    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }

    private void createBooking(String date, String startTime, String endTime, String module) {
        Map<String, Object> booking = new HashMap<>();
        booking.put("studentId", studentId);
        booking.put("tutorId", tutorId);
        booking.put("tutorName", tutorName);
        booking.put("module", module);
        booking.put("date", date);
        booking.put("startTime", startTime);
        booking.put("endTime", endTime);
        booking.put("status", "Pending");
        booking.put("paid", false);

        // Also, check if the student has a pending booking with this tutor to avoid duplicates.
        firestore.collection("bookings")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("tutorId", tutorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(BookingPageActivity.this, "You already have a pending booking with this tutor!", Toast.LENGTH_SHORT).show();
                    } else {
                        firestore.collection("bookings").add(booking)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(BookingPageActivity.this, "Booking request sent!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(BookingPageActivity.this, "Failed to book session: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }
}