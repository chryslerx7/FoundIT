package com.example.foundit.model;

public class LoginRequest {
    public String email;
    public String password;
    public String device_name;

    public LoginRequest(String email, String password, String device_name) {
        this.email = email;
        this.password = password;
        this.device_name = device_name;
    }
}
