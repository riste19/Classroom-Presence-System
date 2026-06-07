package com.example.timska.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.timska.data.local.AppDatabase;
import com.example.timska.data.local.AttendanceDao;
import com.example.timska.data.local.AttendanceEntity;
import com.example.timska.data.remote.ApiService;
import com.example.timska.data.remote.RetrofitClient;
import com.example.timska.data.remote.dto.AttendanceDto;
import com.example.timska.data.remote.dto.AttendanceUploadRequest;
import com.example.timska.data.remote.dto.GenericResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Single source of truth for attendance: writes taps to Room immediately (offline-safe)
 * and uploads pending records to the server on demand.
 */
public class AttendanceRepository {

    public interface InsertCallback {
        void onInserted(boolean added, AttendanceEntity record);
    }

    public interface SyncCallback {
        void onResult(boolean success, int synced, String message);
    }

    private final AttendanceDao dao;
    private final ApiService api;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public AttendanceRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).attendanceDao();
        this.api = RetrofitClient.getApi(context);
    }

    public LiveData<List<AttendanceEntity>> observeAll() {
        return dao.observeAll();
    }

    public LiveData<Integer> observePendingCount() {
        return dao.observePendingCount();
    }

    /**
     * Records a tap. Duplicate taps from the same student since {@code sessionStart} are ignored,
     * which keeps the list clean when a phone is tapped several times in a row.
     */
    public void registerTap(AttendanceEntity record, long sessionStart, InsertCallback cb) {
        io.execute(() -> {
            int existing = dao.countSince(record.studentId, record.teacherId, sessionStart);
            boolean added = false;
            if (existing == 0) {
                record.id = dao.insert(record);
                added = true;
            }
            boolean result = added;
            main.post(() -> cb.onInserted(result, record));
        });
    }

    /** Uploads all unsynced records in one batch and marks them synced on success. */
    public void sync(SyncCallback cb) {
        io.execute(() -> {
            List<AttendanceEntity> pending = dao.getUnsynced();
            if (pending.isEmpty()) {
                main.post(() -> cb.onResult(true, 0, "Нема нови записи за синхронизација"));
                return;
            }

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            List<AttendanceDto> dtos = new ArrayList<>();
            List<Long> ids = new ArrayList<>();
            for (AttendanceEntity e : pending) {
                dtos.add(new AttendanceDto(e.studentId, e.studentName, e.course,
                        e.className, e.teacherId, fmt.format(new Date(e.timestamp))));
                ids.add(e.id);
            }

            try {
                Response<GenericResponse> resp =
                        api.uploadAttendance(new AttendanceUploadRequest(dtos)).execute();
                if (resp.isSuccessful() && resp.body() != null && resp.body().success) {
                    dao.markSynced(ids);
                    main.post(() -> cb.onResult(true, ids.size(), null));
                } else {
                    main.post(() -> cb.onResult(false, 0,
                            "Серверот одби (" + resp.code() + ")"));
                }
            } catch (IOException e) {
                main.post(() -> cb.onResult(false, 0, "Нема конекција — записите остануваат локално"));
            }
        });
    }
}
