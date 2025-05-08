package com.example.dutpeertutoring;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TutorSessionActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private MaterialButton btnSelectPdf, btnUploadPdf;
    private Uri selectedPdfUri;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_session);

        firestore = FirebaseFirestore.getInstance();
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnUploadPdf = findViewById(R.id.btnUploadPdf);

        btnSelectPdf.setOnClickListener(view -> openFileChooser());
        btnUploadPdf.setOnClickListener(view -> {
            if (selectedPdfUri != null) {
                uploadPdfToFirestore(selectedPdfUri);
            } else {
                Toast.makeText(TutorSessionActivity.this, "Please select a PDF file first", Toast.LENGTH_SHORT).show();
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
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
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
            String base64Pdf = android.util.Base64.encodeToString(pdfBytes, android.util.Base64.DEFAULT);

            Map<String, Object> data = new HashMap<>();
            data.put("pdfBase64", base64Pdf);
            data.put("timestamp", FieldValue.serverTimestamp());
            data.put("tutorId", FirebaseAuth.getInstance().getCurrentUser().getUid());

            firestore.collection("resources")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(TutorSessionActivity.this, "PDF uploaded successfully!", Toast.LENGTH_SHORT).show();
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