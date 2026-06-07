package com.example.timska.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timska.data.local.AttendanceEntity;
import com.example.timska.data.prefs.SessionManager;
import com.example.timska.data.repository.AttendanceRepository;
import com.example.timska.nfc.StudentPayload;

import java.util.List;
import java.util.Random;

public class TeacherViewModel extends AndroidViewModel {

    private final AttendanceRepository repository;
    private final SessionManager session;

    private final LiveData<List<AttendanceEntity>> records;
    private final LiveData<Integer> pendingCount;
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sessionActive = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);

    /** Start of the active session — used to ignore duplicate taps within the same session. */
    private long sessionStart = System.currentTimeMillis();

    private final Random random = new Random();
    private static final String[][] DEMO_STUDENTS = {
            {"201234", "Ана Стоилкова", "Мобилни апликации"},
            {"201235", "Бојан Петров", "Мобилни апликации"},
            {"201236", "Викторија Илиева", "Веб програмирање"},
            {"201237", "Дарко Николов", "Бази на податоци"},
    };

    public TeacherViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AttendanceRepository(application);
        this.session = new SessionManager(application);
        this.records = repository.observeAll();
        this.pendingCount = repository.observePendingCount();
    }

    public LiveData<List<AttendanceEntity>> getRecords() {
        return records;
    }

    public LiveData<Integer> getPendingCount() {
        return pendingCount;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getSessionActive() {
        return sessionActive;
    }

    public LiveData<Boolean> getSyncing() {
        return syncing;
    }

    public void startSession() {
        sessionStart = System.currentTimeMillis();
        sessionActive.setValue(true);
    }

    public void endSession() {
        sessionActive.setValue(false);
    }

    public boolean isSessionActive() {
        return Boolean.TRUE.equals(sessionActive.getValue());
    }

    /** Called from both the real NFC reader and the "Simulate tap" button. */
    public void onStudentRead(StudentPayload payload) {
        if (!isSessionActive()) {
            message.setValue("Сесијата е завршена — започни нова за да примаш tap-ови");
            return;
        }
        String className = session.getCourse() == null || session.getCourse().isEmpty()
                ? "Предавање" : session.getCourse();
        AttendanceEntity record = new AttendanceEntity(
                payload.studentId,
                payload.studentName,
                payload.course,
                className,
                session.getUserId(),
                System.currentTimeMillis());

        repository.registerTap(record, sessionStart, (added, saved) -> {
            if (added) {
                message.setValue("Регистриран: " + saved.studentName);
            } else {
                message.setValue(saved.studentName + " е веќе регистриран во оваа сесија");
            }
        });
    }

    public void simulateTap() {
        String[] demo = DEMO_STUDENTS[random.nextInt(DEMO_STUDENTS.length)];
        onStudentRead(new StudentPayload(demo[0], demo[1], demo[2]));
    }

    public void sync() {
        syncing.setValue(true);
        repository.sync((success, synced, msg) -> {
            syncing.setValue(false);
            if (success) {
                message.setValue(synced == 0
                        ? "Нема нови записи за синхронизација"
                        : "Синхронизирани " + synced + " записи ✓");
            } else {
                message.setValue(msg);
            }
        });
    }
}
