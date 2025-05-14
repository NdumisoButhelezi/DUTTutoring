package com.example.dutpeertutoring;

import android.graphics.Bitmap;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Tutor {
    private String id;
    private String name;
    private String surname;
    private List<String> modules;
    private boolean profileComplete;
    private String role;
    private boolean isConfirmed;
    private boolean approved; // Field to track approval status
    private String email;
    private Bitmap profileImageBitmap;
    private String profileImageBase64;
    private String academicRecordBase64;
    private String status;
    private double rating;
    private float averageRating;

    // Default constructor (required for Firestore)
    public Tutor() {}

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    // Getters and setters
    public String getAcademicRecordBase64() {
        return academicRecordBase64;
    }

    public String getSurname() {
        return surname;
    }
    public double getRating() {
        return rating;
    }

    public Tutor(String id, float averageRating) {
        this.id = id;
        this.averageRating = averageRating;
    }


    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isApproved() { // Getter for approval status
        return approved;
    }

    public void setApproved(boolean approved) { // Setter for approval status
        this.approved = approved;
    }

    public Bitmap getProfileImageBitmap() {
        return profileImageBitmap;
    }

    public void setProfileImageBitmap(Bitmap profileImageBitmap) {
        this.profileImageBitmap = profileImageBitmap;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }

    public void setAcademicRecordBase64(String academicRecordBase64) {
        this.academicRecordBase64 = academicRecordBase64;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @PropertyName("isConfirmed")
    public boolean isConfirmed() {
        return isConfirmed;
    }

    @PropertyName("isConfirmed")
    public void setConfirmed(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getAverageRating() {
        return averageRating;
    }
}