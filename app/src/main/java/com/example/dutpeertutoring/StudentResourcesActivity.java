package com.example.dutpeertutoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentResourcesActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private RecyclerView recyclerView;
    private ResourcesAdapter adapter;
    private FirebaseFirestore firestore;
    private Button btnSaveRatings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_resources);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResourcesAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        btnSaveRatings = findViewById(R.id.btnSaveRatings);
        btnSaveRatings.setOnClickListener(v -> {
            adapter.saveRatingsToFirestore();
        });

        // Request storage permission for downloads on Android 6.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            } else {
                fetchHistoricalBookings();
            }
        } else {
            fetchHistoricalBookings();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchHistoricalBookings();
            } else {
                Toast.makeText(this, "Storage permission is needed to download PDFs.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Fetch tutors associated with historical (paid) bookings.
     */
    private void fetchHistoricalBookings() {
        String studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("bookings")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("status", "Approved:Paid")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> tutorIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tutorId = doc.getString("tutorId");
                        if (tutorId != null && !tutorIds.contains(tutorId)) {
                            tutorIds.add(tutorId);
                        }
                    }
                    if (!tutorIds.isEmpty()) {
                        fetchResourcesForTutors(tutorIds);
                    } else {
                        Toast.makeText(this, "No tutors found for your bookings.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load historical bookings", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetch resources uploaded by tutors who are linked to historical bookings.
     * This method fetches each resource and enriches with tutor name from "users" collection.
     */
    private void fetchResourcesForTutors(List<String> tutorIds) {
        firestore.collection("resources")
                .whereIn("tutorId", tutorIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Resource> resources = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Resource resource = doc.toObject(Resource.class);
                        resource.setId(doc.getId());

                        // Fetch tutor name for each resource asynchronously
                        String tutorId = resource.getTutorId();
                        firestore.collection("users").document(tutorId).get()
                                .addOnSuccessListener(tutorDoc -> {
                                    if (tutorDoc.exists()) {
                                        String name = tutorDoc.getString("name");
                                        String surname = tutorDoc.getString("surname");
                                        resource.setTutorName((name != null ? name : "") + " " + (surname != null ? surname : ""));
                                    } else {
                                        resource.setTutorName("Unknown Tutor");
                                    }
                                    resources.add(resource);

                                    // Once all resources processed, update adapter
                                    if (resources.size() == queryDocumentSnapshots.size()) {
                                        adapter.updateResources(resources);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    resource.setTutorName("Unknown Tutor");
                                    resources.add(resource);
                                    if (resources.size() == queryDocumentSnapshots.size()) {
                                        adapter.updateResources(resources);
                                    }
                                });
                    }

                    // Handle empty case if no resources found
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No resources found for your tutors.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load resources", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to decode and save the PDF
    private void saveDecodedPdf(String base64EncodedPdf, String filename) {
        byte[] decodedBytes = Base64.decode(base64EncodedPdf, Base64.DEFAULT);
        try {
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            fos.write(decodedBytes);
            fos.close();
            Toast.makeText(this, "PDF saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Method to preview PDF
    private void previewPdf(String filename) {
        File file = new File(getFilesDir(), filename);
        Uri pdfUri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    // Method to download PDF
    public  void downloadPdf(String base64EncodedPdf, String filename) {
        saveDecodedPdf(base64EncodedPdf, filename);
        previewPdf(filename);
    }
}
