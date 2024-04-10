package com.example.attendance;
public class StudentItem {
    private String roll;
    private String name;
    private String status;
    private String key;


    public StudentItem() {

    }
    public StudentItem(String roll, String name, String status, String key) {
        this.roll = roll;
        this.name = name;
        this.status = status;
        this.key = key;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {

        this.roll = roll;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }
}
