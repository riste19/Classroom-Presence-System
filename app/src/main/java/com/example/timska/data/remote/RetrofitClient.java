package com.example.timska.data.remote;

import android.content.Context;

import com.example.timska.data.prefs.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    /**
     * Base URL of the PHP backend.
     * 10.0.2.2 is the host machine as seen from the Android emulator, so this targets
     * XAMPP's htdocs/cps on the developer's computer. Change to the LAN IP for a real device.
     */
    public static final String BASE_URL = "http://10.0.2.2/cps/";

    private static volatile ApiService apiService;

    private RetrofitClient() {
    }

    public static ApiService getApi(Context context) {
        if (apiService == null) {
            synchronized (RetrofitClient.class) {
                if (apiService == null) {
                    apiService = build(context.getApplicationContext());
                }
            }
        }
        return apiService;
    }

    private static ApiService build(Context appContext) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(new SessionManager(appContext)))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }
}
