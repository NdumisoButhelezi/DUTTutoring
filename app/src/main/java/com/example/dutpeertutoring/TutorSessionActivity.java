package com.example.dutpeertutoring;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TutorSessionActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private Button uploadPdfButton, startSessionButton;
    private Uri selectedPdfUri;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_session);

        firestore = FirebaseFirestore.getInstance();
        uploadPdfButton = findViewById(R.id.uploadPdfButton);
        startSessionButton = findViewById(R.id.startSessionButton);

        uploadPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        startSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPdfUri != null) {
                    uploadPdfToFirestore(selectedPdfUri);
                } else {
                    Toast.makeText(TutorSessionActivity.this, "Please select a PDF file first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedPdfUri = data.getData();
            Toast.makeText(this, "PDF Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPdfToFirestore(Uri pdfUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] pdfBytes = byteBuffer.toByteArray();
            // Encode the PDF bytes to a Base64 string
            String base64Pdf = Base64.encodeToString(pdfBytes, Base64.DEFAULT);

            Map<String, Object> data = new HashMap<>();
            data.put("pdfBase64", base64Pdf);
            data.put("timestamp", FieldValue.serverTimestamp());

            // Generate a new document ID or use a custom ID based on session details
            String docId = "session_" + System.currentTimeMillis();
            firestore.collection("resources")
                    .document(docId)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(TutorSessionActivity.this, "PDF uploaded & session started!", Toast.LENGTH_SHORT).show();
                        // Optionally, trigger a student-side process or navigation.
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(TutorSessionActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}