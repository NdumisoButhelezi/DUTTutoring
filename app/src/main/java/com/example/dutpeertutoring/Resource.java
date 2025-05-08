package com.example.dutpeertutoring;

public class Resource {
    private String id;       // Unique identifier for the resource
    private String tutorId;  // ID of the tutor who uploaded the resource

    // Default constructor (required for Firestore)
    public Resource() {}

    // Constructor to initialize the resource
    public Resource(String id, String tutorId) {
        this.id = id;
        this.tutorId = tutorId;
    }

    // Getter for the resource ID
    public String getId() {
        return id;
    }

    // Setter for the resource ID
    public void setId(String id) {
        this.id = id;
    }

    // Getter for the tutor ID
    public String getTutorId() {
        return tutorId;
    }

    // Setter for the tutor ID
    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }
}