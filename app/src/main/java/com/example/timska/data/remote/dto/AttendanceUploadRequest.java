package com.example.timska.data.remote.dto;

import java.util.List;

/** Body of POST /api/attendance.php — a batch of records for bulk sync. */
public class AttendanceUploadRequest {
    public List<AttendanceDto> records;

    public AttendanceUploadRequest(List<AttendanceDto> records) {
        this.records = records;
    }
}
