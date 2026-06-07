package com.example.timska.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timska.R;
import com.example.timska.data.prefs.SessionManager;
import com.example.timska.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Already logged in? Skip straight to the right home screen.
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            routeByRole(session.getRole());
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        binding.toggleRole.check(R.id.btnTeacher);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        viewModel.getLoading().observe(this, this::showLoading);
        viewModel.getError().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getLoggedInRole().observe(this, role -> {
            if (role != null) {
                routeByRole(role);
                finish();
            }
        });
    }

    private void attemptLogin() {
        String username = text(binding.etUsername);
        String password = text(binding.etPassword);
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.login_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String role = binding.toggleRole.getCheckedButtonId() == R.id.btnStudent
                ? SessionManager.ROLE_STUDENT : SessionManager.ROLE_TEACHER;
        viewModel.login(username, password, role);
    }

    private void routeByRole(String role) {
        Class<?> target = SessionManager.ROLE_STUDENT.equalsIgnoreCase(role)
                ? StudentActivity.class : TeacherActivity.class;
        startActivity(new Intent(this, target));
    }

    private void showLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }

    private String text(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
