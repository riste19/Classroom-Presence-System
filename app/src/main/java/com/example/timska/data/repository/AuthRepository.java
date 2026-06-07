package com.example.timska.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.timska.data.prefs.SessionManager;
import com.example.timska.data.remote.ApiService;
import com.example.timska.data.remote.RetrofitClient;
import com.example.timska.data.remote.dto.LoginRequest;
import com.example.timska.data.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public interface LoginCallback {
        void onSuccess(String role);
        void onError(String message);
    }

    private final ApiService api;
    private final SessionManager session;

    public AuthRepository(Context context) {
        this.api = RetrofitClient.getApi(context);
        this.session = new SessionManager(context);
    }

    public void login(String username, String password, String role, LoginCallback cb) {
        api.login(new LoginRequest(username, password, role)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,
                                   @NonNull Response<LoginResponse> response) {
                LoginResponse body = response.body();
                if (response.isSuccessful() && body != null && body.success
                        && body.token != null && body.user != null) {
                    session.save(body.token, body.user);
                    cb.onSuccess(body.user.role);
                } else {
                    String msg = body != null && body.message != null
                            ? body.message : "Неуспешна најава (" + response.code() + ")";
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                cb.onError("Грешка во конекција: " + t.getMessage());
            }
        });
    }
}
