package com.example.managehairfinal.model;

import java.util.HashMap;
import java.util.Map;

public class userData {
    public String full_name;
    public String phone_num;
    public String email;
    public String password;
    public String uid;
    public boolean admin;

    public userData(String full_name, String phone_num, String email, String password, String uid, boolean admin) {
        this.full_name = full_name;
        this.phone_num = phone_num;
        this.email = email;
        this.password = password;
        this.uid = uid;
        this.admin = admin;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String name) {
        full_name = name;
    }

    public String getPhone_num() {
        return phone_num;
    }
    public void setPhone_num(String number) {
        phone_num = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String mail) {
        email = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pass) {
        password = pass;
    }

    public String getUid() {
        return uid;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean getAdmin(){
        return admin;
    }

    public userData(){}

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("full_name", full_name);
        result.put("phone_num", phone_num);
        return result;
    }

}
