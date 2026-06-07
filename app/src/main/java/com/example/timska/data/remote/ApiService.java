package com.example.timska.data.remote;

import com.example.timska.data.remote.dto.AttendanceUploadRequest;
import com.example.timska.data.remote.dto.GenericResponse;
import com.example.timska.data.remote.dto.LoginRequest;
import com.example.timska.data.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/login.php")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/attendance.php")
    Call<GenericResponse> uploadAttendance(@Body AttendanceUploadRequest request);
}
