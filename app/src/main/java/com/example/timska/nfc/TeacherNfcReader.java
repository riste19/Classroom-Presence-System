package com.example.timska.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;

/**
 * Reader-mode callback for the teacher device. On each tap it selects our AID over ISO-DEP,
 * reads the student payload, and delivers the result on the main thread.
 */
public class TeacherNfcReader implements NfcAdapter.ReaderCallback {

    public interface Listener {
        void onStudentRead(StudentPayload payload);
        void onReadError(String message);
    }

    /** Flags for {@link NfcAdapter#enableReaderMode}. */
    public static final int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A
                    | NfcAdapter.FLAG_READER_NFC_B
                    | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());

    public TeacherNfcReader(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            post(() -> listener.onReadError("Уредот не поддржува ISO-DEP"));
            return;
        }
        try {
            isoDep.connect();
            byte[] response = isoDep.transceive(NfcProtocol.buildSelectApdu());
            if (response != null && NfcProtocol.endsWithOk(response) && response.length > 2) {
                byte[] tokenBytes = Arrays.copyOf(response, response.length - 2);
                // Verify the signed token (rejects tampered or stale payloads) before trusting it.
                byte[] payloadBytes = SecureToken.unwrap(tokenBytes);
                StudentPayload payload = StudentPayload.fromBytes(payloadBytes);
                post(() -> listener.onStudentRead(payload));
            } else {
                post(() -> listener.onReadError("Невалиден одговор од студентскиот телефон"));
            }
        } catch (Exception e) {
            post(() -> listener.onReadError("Грешка при читање: " + e.getMessage()));
        } finally {
            try {
                isoDep.close();
            } catch (Exception ignored) {
                // already closed
            }
        }
    }

    private void post(Runnable r) {
        main.post(r);
    }
}
