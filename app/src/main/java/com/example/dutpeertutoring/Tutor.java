package com.example.dutpeertutoring;

import java.util.List;

public class Tutor {

    private String id;
    private String name;
    private List<String> modules;
    private boolean isConfirmed;

    // Default constructor (required for Firestore deserialization)
    public Tutor() {
    }

    // Constructor with parameters
    public Tutor(String id, String name, List<String> modules, boolean isConfirmed) {
        this.id = id;
        this.name = name;
        this.modules = modules;
        this.isConfirmed = isConfirmed;
    }

    // Getters and Setters
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

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }
}