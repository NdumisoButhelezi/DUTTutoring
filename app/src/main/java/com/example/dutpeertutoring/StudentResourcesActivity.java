package com.example.dutpeertutoring;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StudentResourcesActivity extends AppCompatActivity {

    private MaterialTextView tvResourceTitle;
    private MaterialButton btnDownload, btnRating;
    private FirebaseFirestore firestore;
    // For demonstration, we assume a minimum delay (e.g., one hour) must pass before rating can be done.
    private static final long RATING_AVAILABLE_DELAY_MILLIS = 60 * 60 * 1000; // 1 hour delay

    // Variables to store the fetched resource data.
    private String resourceDocumentId;
    private String pdfBase64;
    private Timestamp resourceTimestamp;
    private double currentRating; // You can pull this from the resource document if available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_resources);

        tvResourceTitle = findViewById(R.id.tvResourceTitle);
        btnDownload = findViewById(R.id.btnDownload);
        btnRating = findViewById(R.id.btnRating);

        firestore = FirebaseFirestore.getInstance();

        // Initially disable the rating button.
        btnRating.setEnabled(false);

        fetchResource();

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadPdf();
            }
        });

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });
    }

    private void fetchResource() {
        // For demonstration, fetch the latest resource from the "resources" collection.
        firestore.collection("resources")
                .orderBy("timestamp")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        resourceDocumentId = doc.getId();
                        pdfBase64 = doc.getString("pdfBase64");
                        resourceTimestamp = doc.getTimestamp("timestamp");

                        // Set a title using the document id or any field available.
                        tvResourceTitle.setText("Resource: " + resourceDocumentId);

                        // Check if rating should be available.
                        if (resourceTimestamp != null) {
                            long resourceTimeMillis = resourceTimestamp.toDate().getTime();
                            long currentTimeMillis = System.currentTimeMillis();
                            if (currentTimeMillis - resourceTimeMillis >= RATING_AVAILABLE_DELAY_MILLIS) {
                                btnRating.setEnabled(true);
                            } else {
                                btnRating.setEnabled(false);
                            }
                        }
                    } else {
                        Toast.makeText(StudentResourcesActivity.this, "No resources available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(StudentResourcesActivity.this, "Failed to load resources: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("StudentResources", "Error fetching resource", e);
                });
    }

    private void downloadPdf() {
        if (pdfBase64 == null || pdfBase64.isEmpty()) {
            Toast.makeText(this, "No PDF data available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] pdfBytes = Base64.decode(pdfBase64, Base64.DEFAULT);
            // Save the PDF file to external storage directory.
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            // Create a file name based on resource id.
            File pdfFile = new File(downloadsDir, resourceDocumentId + ".pdf");

            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(pdfBytes);
            fos.close();

            Toast.makeText(this, "PDF downloaded to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Open the PDF file.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StudentResourcesActivity.this);
        builder.setTitle("Rate Tutor");

        // Inflate a custom layout for the rating dialog.
        View dialogView = LayoutInflater.from(StudentResourcesActivity.this).inflate(R.layout.dialog_rating, null);
        final RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        // Optionally set current rating value.
        ratingBar.setRating((float) currentRating);

        builder.setView(dialogView);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                float rating = ratingBar.getRating();
                submitRating(rating);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void submitRating(float rating) {
        // Logic to update the rating in Firestore.
        // Optionally, you may update a tutor's rating or this resource document with the rating.
        firestore.collection("resources")
                .document(resourceDocumentId)
                .update("rating", rating)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(StudentResourcesActivity.this, "Rating submitted: " + rating, Toast.LENGTH_SHORT).show();
                    // Disable rating button after rating.
                    btnRating.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(StudentResourcesActivity.this, "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}