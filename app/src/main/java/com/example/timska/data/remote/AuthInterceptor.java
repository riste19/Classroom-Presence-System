package com.example.timska.data.remote;

import androidx.annotation.NonNull;

import com.example.timska.data.prefs.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Attaches the stored JWT as a Bearer token to every request once the user is logged in. */
public class AuthInterceptor implements Interceptor {

    private final SessionManager session;

    public AuthInterceptor(SessionManager session) {
        this.session = session;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }
        Request authorized = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authorized);
    }
}
