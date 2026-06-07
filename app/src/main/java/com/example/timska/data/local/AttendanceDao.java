package com.example.timska.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert
    long insert(AttendanceEntity record);

    /** Observed by the teacher screen — newest taps first. */
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    LiveData<List<AttendanceEntity>> observeAll();

    /** Live count of records still waiting to be uploaded. */
    @Query("SELECT COUNT(*) FROM attendance WHERE synced = 0")
    LiveData<Integer> observePendingCount();

    @Query("SELECT * FROM attendance WHERE synced = 0 ORDER BY timestamp ASC")
    List<AttendanceEntity> getUnsynced();

    @Query("UPDATE attendance SET synced = 1 WHERE id IN (:ids)")
    void markSynced(List<Long> ids);

    /** Used to drop duplicate taps from the same student within the active session. */
    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId "
            + "AND teacherId = :teacherId AND timestamp >= :since")
    int countSince(String studentId, String teacherId, long since);
}
