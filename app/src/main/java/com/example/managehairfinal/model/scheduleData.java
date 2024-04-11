package com.example.managehairfinal.model;

import java.util.List;

public class scheduleData {
    String day;
    List<String> hours;

    public scheduleData(String day, List<String> hours){
        this.day = day;
        this.hours = hours;
    }

    public scheduleData() {
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDay() {
        return day;
    }

    public void setHours(List<String> hours) {
        this.hours = hours;
    }

    public List<String> getHours() {
        return hours;
    }
}
