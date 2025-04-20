package com.example.dutpeertutoring;

public class Booking {

    private String id;
    private String studentId;
    private String tutorId;
    private String module;
    private String dateTime;
    private String status;

    // Default constructor (required for Firestore deserialization)
    public Booking() {
    }

    // Constructor with parameters
    public Booking(String id, String studentId, String tutorId, String module, String dateTime, String status) {
        this.id = id;
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.module = module;
        this.dateTime = dateTime;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}