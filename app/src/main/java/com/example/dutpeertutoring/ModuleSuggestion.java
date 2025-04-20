package com.example.dutpeertutoring;

import java.util.Map;

public class ModuleSuggestion {

    private String id; // Firestore document ID
    private String moduleName;
    private String studentId; // ID of the student who suggested the module
    private Map<String, Boolean> votes; // Map of student IDs to their votes
    private String status; // Status of the suggestion (e.g., Pending, Approved, Rejected)

    // Default constructor (required for Firestore deserialization)
    public ModuleSuggestion() {
    }

    // Constructor with parameters
    public ModuleSuggestion(String moduleName, String studentId, Map<String, Boolean> votes, String status) {
        this.moduleName = moduleName;
        this.studentId = studentId;
        this.votes = votes;
        this.status = status;
    }

    // Getter and Setter for ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for Module Name
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    // Getter and Setter for Student ID
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    // Getter and Setter for Votes
    public Map<String, Boolean> getVotes() {
        return votes;
    }

    public void setVotes(Map<String, Boolean> votes) {
        this.votes = votes;
    }

    // Getter and Setter for Status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Method to calculate the total vote count
    public int getVoteCount() {
        if (votes == null) {
            return 0;
        }
        int count = 0;
        for (Boolean vote : votes.values()) {
            if (vote) { // Count upvotes only
                count++;
            }
        }
        return count;
    }
}