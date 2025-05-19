package com.example.dutpeertutoring;
public class Resource {
    private String id;         // Firestore document ID
    private String tutorId;    // Tutor UID
    private String title;      // Resource title
    private String pdfData;    // Base64 encoded PDF data
    private String tutorName;  // Tutor full name
    private float rating;      // Rating for the resource

    // Default constructor required by Firestore
    public Resource() {}

    public Resource(String id, String tutorId, String title, String pdfData, String tutorName, float rating) {
        this.id = id;
        this.tutorId = tutorId;
        this.title = title;
        this.pdfData = pdfData;
        this.tutorName = tutorName;
        this.rating = rating;
    }

    // Getter and Setter methods
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPdfData() { return pdfData; }
    public void setPdfData(String pdfData) { this.pdfData = pdfData; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}
