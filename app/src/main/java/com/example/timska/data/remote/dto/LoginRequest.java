package com.example.timska.data.remote.dto;

public class LoginRequest {
    public String username;
    public String password;
    public String role;

    public LoginRequest(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
