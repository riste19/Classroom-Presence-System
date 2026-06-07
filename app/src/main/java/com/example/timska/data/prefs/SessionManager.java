package com.example.timska.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.timska.data.remote.dto.LoginResponse;

/** Stores the logged-in user's session (JWT + profile) in SharedPreferences. */
public class SessionManager {

    private static final String PREFS = "cps_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_COURSE = "course";

    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(String token, LoginResponse.User user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, user.role)
                .putString(KEY_USER_ID, user.id)
                .putString(KEY_FULL_NAME, user.fullName)
                .putString(KEY_STUDENT_ID, user.studentId)
                .putString(KEY_COURSE, user.course)
                .apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getStudentId() {
        return prefs.getString(KEY_STUDENT_ID, "");
    }

    public String getCourse() {
        return prefs.getString(KEY_COURSE, "");
    }

    public boolean isTeacher() {
        return ROLE_TEACHER.equalsIgnoreCase(getRole());
    }
}
