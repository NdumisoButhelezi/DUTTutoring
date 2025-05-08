package com.example.dutpeertutoring;

public class Rating {
    private String tutorId;
    private float rating;

    // Default constructor (required for Firestore)
    public Rating() {}

    // Constructor to initialize the rating
    public Rating(String tutorId, float rating) {
        this.tutorId = tutorId;
        this.rating = rating;
    }

    // Getter for tutor ID
    public String getTutorId() {
        return tutorId;
    }

    // Setter for tutor ID
    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    // Getter for rating
    public float getRating() {
        return rating;
    }

    // Setter for rating
    public void setRating(float rating) {
        this.rating = rating;
    }
}