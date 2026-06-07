package com.example.timska.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire format for a single attendance record sent to the server. */
public class AttendanceDto {

    @SerializedName("student_id")
    public String studentId;

    @SerializedName("student_name")
    public String studentName;

    public String course;

    @SerializedName("class_name")
    public String className;

    @SerializedName("teacher_id")
    public String teacherId;

    /** "yyyy-MM-dd HH:mm:ss" — ready for a MySQL DATETIME column. */
    public String timestamp;

    public AttendanceDto(String studentId, String studentName, String course,
                         String className, String teacherId, String timestamp) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.course = course;
        this.className = className;
        this.teacherId = teacherId;
        this.timestamp = timestamp;
    }
}
