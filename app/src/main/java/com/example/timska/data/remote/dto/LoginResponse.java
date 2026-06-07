package com.example.timska.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Response shape of POST /api/login.php */
public class LoginResponse {
    public boolean success;
    public String message;
    public String token;

    @SerializedName("user")
    public User user;

    public static class User {
        public String id;

        @SerializedName("full_name")
        public String fullName;

        public String role;

        @SerializedName("student_id")
        public String studentId;

        public String course;
    }
}
