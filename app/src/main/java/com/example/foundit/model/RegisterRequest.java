package com.example.foundit.model;

public class RegisterRequest {
    public String name;
    public String student_id;
    public String email;
    public String password;
    public String password_confirmation;

    public RegisterRequest(String name, String student_id, String email, String password, String password_confirmation) {
        this.name = name;
        this.student_id = student_id;
        this.email = email;
        this.password = password;
        this.password_confirmation = password_confirmation;
    }
}
