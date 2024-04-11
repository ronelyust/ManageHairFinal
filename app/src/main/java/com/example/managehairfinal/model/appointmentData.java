package com.example.managehairfinal.model;

// Appointment.java
public class appointmentData {
    private String date;
    private String time;
    private boolean available;
    private String userName;
    private String phoneNumber;

    private String uid;

    public appointmentData() {
        // Default constructor required for Firebase
    }

    public appointmentData(String date, String time, boolean available) {
        this.date = date;
        this.time = time;
        this.available = available;
    }

    public appointmentData(String date, String time, boolean available, String name, String phone, String uid) {
        this.date = date;
        this.time = time;
        this.available = available;
        this.userName = name;
        this.phoneNumber = phone;
        this.uid = uid;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setUid(String uid){this.uid = uid;}

    public String getUid() {
        return uid;
    }
}
