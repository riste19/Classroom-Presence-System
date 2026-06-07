package com.example.timska.ui;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.timska.R;
import com.example.timska.data.prefs.SessionManager;
import com.example.timska.databinding.ActivityTeacherBinding;
import com.example.timska.nfc.StudentPayload;
import com.example.timska.nfc.TeacherNfcReader;
import com.example.timska.util.Feedback;

/**
 * Teacher home screen. While a session is active the phone is put into NFC reader mode and each
 * student tap is read, stored in Room, and shown live. Records sync to the server on demand.
 */
public class TeacherActivity extends AppCompatActivity implements TeacherNfcReader.Listener {

    private ActivityTeacherBinding binding;
    private TeacherViewModel viewModel;
    private SessionManager session;

    private NfcAdapter nfcAdapter;
    private TeacherNfcReader nfcReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(TeacherViewModel.class);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcReader = new TeacherNfcReader(this);

        AttendanceAdapter adapter = new AttendanceAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.tvTitle.setText(getString(R.string.teacher_title) + " · " + session.getFullName());

        viewModel.getRecords().observe(this, adapter::submitList);
        viewModel.getPendingCount().observe(this, count ->
                binding.tvPending.setText(getString(R.string.pending_count, count == null ? 0 : count)));
        viewModel.getSessionActive().observe(this, this::renderSessionState);
        viewModel.getMessage().observe(this, this::onMessage);
        viewModel.getSyncing().observe(this, this::renderSyncing);

        binding.btnSync.setOnClickListener(v -> viewModel.sync());
        binding.btnSimulate.setOnClickListener(v -> viewModel.simulateTap());
        binding.btnEndSession.setOnClickListener(v -> toggleSession());
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void toggleSession() {
        if (viewModel.isSessionActive()) {
            viewModel.endSession();
        } else {
            viewModel.startSession();
        }
        updateReaderMode();
    }

    private void renderSyncing(boolean syncing) {
        binding.syncProgress.setVisibility(syncing ? View.VISIBLE : View.GONE);
        binding.btnSync.setEnabled(!syncing);
    }

    private void renderSessionState(boolean active) {
        binding.tvStatus.setText(active ? R.string.session_active : R.string.session_ended);
        binding.btnEndSession.setText(active ? R.string.action_end_session : R.string.action_start_session);
    }

    private void onMessage(String msg) {
        if (msg == null) {
            return;
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (msg.startsWith("Регистриран")) {
            Feedback.success(this);
        }
    }

    private void logout() {
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- NFC reader lifecycle ---

    @Override
    protected void onResume() {
        super.onResume();
        updateReaderMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    private void updateReaderMode() {
        boolean nfcReady = nfcAdapter != null && nfcAdapter.isEnabled();
        binding.tvNfcWarning.setVisibility(nfcReady ? View.GONE : View.VISIBLE);
        if (nfcAdapter == null) {
            return;
        }
        if (nfcReady && viewModel.isSessionActive()) {
            nfcAdapter.enableReaderMode(this, nfcReader, TeacherNfcReader.READER_FLAGS, null);
        } else {
            nfcAdapter.disableReaderMode(this);
        }
    }

    // --- TeacherNfcReader.Listener (called on the main thread) ---

    @Override
    public void onStudentRead(StudentPayload payload) {
        viewModel.onStudentRead(payload);
    }

    @Override
    public void onReadError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Feedback.error(this);
    }
}
