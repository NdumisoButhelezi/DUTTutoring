package com.example.dutpeertutoring;

import java.util.List;

public class Tutor {
    private String id; // Add this field
    private String name;
    private String surname;
    private List<String> modules;
    private boolean profileComplete;
    private String role;
    private boolean isConfirmed;
    private String email;

    // Default constructor (required for Firestore)
    public Tutor() {}

    // Getters and setters
    public String getId() { // Add this method
        return id;
    }

    public void setId(String id) { // Add this method
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
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

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}