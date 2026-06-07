package com.example.timska.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.timska.R;
import com.example.timska.data.prefs.SessionManager;
import com.example.timska.databinding.ActivityStudentBinding;
import com.example.timska.nfc.StudentApduService;
import com.example.timska.util.Feedback;

/**
 * Student home screen. The phone is already acting as an NFC card (via {@link StudentApduService});
 * this screen just shows readiness and reacts when a tap is read by a teacher.
 */
public class StudentActivity extends AppCompatActivity {

    private ActivityStudentBinding binding;
    private SessionManager session;

    private final BroadcastReceiver tapReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPresenceSent();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        binding.tvWho.setText(session.getFullName() + " · " + session.getStudentId());
        binding.tvPayload.setText(getString(R.string.student_payload_info,
                session.getStudentId() + " — " + session.getFullName()));

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void onPresenceSent() {
        binding.tvStatus.setText(R.string.payload_sent);
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
        Feedback.success(this);
    }

    private void logout() {
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(StudentApduService.ACTION_PAYLOAD_SENT);
        ContextCompat.registerReceiver(this, tapReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
        showNfcWarningIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(tapReceiver);
    }

    private void showNfcWarningIfNeeded() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        boolean nfcReady = adapter != null && adapter.isEnabled();
        binding.tvNfcWarning.setVisibility(nfcReady ? View.GONE : View.VISIBLE);
    }
}
