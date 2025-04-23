package com.example.dutpeertutoring;

public class Booking {
    private String id;
    private String studentId;
    private String tutorId;
    private String module;
    private String status; // "Pending", "Cancelled", "Approved:WaitingPayment", "Approved:Paid"
    private String date; // Format: "YYYY-MM-DD"
    private String startTime; // Format: "HH:mm"
    private String endTime;   // Format: "HH:mm"
    private boolean paid;

    public Booking() {}

    public Booking(String id, String studentId, String tutorId, String module, String status,
                   String date, String startTime, String endTime, boolean paid) {
        this.id = id;
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.module = module;
        this.status = status;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.paid = paid;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}