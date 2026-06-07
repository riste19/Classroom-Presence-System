package com.example.timska.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * A single attendance record stored locally on the teacher's device.
 * Records are created when a student taps (NFC) and uploaded to the server during sync.
 */
@Entity(tableName = "attendance")
public class AttendanceEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String studentId = "";

    @NonNull
    public String studentName = "";

    public String course;

    public String className;

    @NonNull
    public String teacherId = "";

    /** Epoch milliseconds when the tap was registered. */
    public long timestamp;

    /** false = still pending upload, true = already synced to the server. */
    public boolean synced;

    public AttendanceEntity() {
    }

    @Ignore
    public AttendanceEntity(@NonNull String studentId, @NonNull String studentName, String course,
                            String className, @NonNull String teacherId, long timestamp) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.course = course;
        this.className = className;
        this.teacherId = teacherId;
        this.timestamp = timestamp;
        this.synced = false;
    }
}
