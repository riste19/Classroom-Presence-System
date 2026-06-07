package com.example.timska.nfc;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import com.example.timska.data.prefs.SessionManager;

/**
 * HCE service: makes the student's phone behave like an NFC smart card.
 *
 * When the teacher's reader selects our AID, we answer with the logged-in student's payload.
 * The service is declared in the manifest with an {@code apduservice} meta-data resource.
 */
public class StudentApduService extends HostApduService {

    /** Broadcast sent to the student UI after a successful tap, for on-screen feedback. */
    public static final String ACTION_PAYLOAD_SENT = "com.example.timska.PAYLOAD_SENT";

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (!NfcProtocol.isSelectAidApdu(commandApdu)) {
            return NfcProtocol.SW_ERROR;
        }

        SessionManager session = new SessionManager(this);
        String studentId = session.getStudentId();
        if (!session.isLoggedIn() || studentId == null || studentId.isEmpty()) {
            // Not logged in as a student — nothing to broadcast.
            return NfcProtocol.SW_ERROR;
        }

        StudentPayload payload = new StudentPayload(
                studentId, session.getFullName(), session.getCourse());

        // Broadcast a freshly signed, time-limited token rather than the raw student ID.
        byte[] token = SecureToken.wrap(payload.toBytes());

        notifyTapSent();
        return NfcProtocol.concat(token, NfcProtocol.SW_OK);
    }

    private void notifyTapSent() {
        Intent intent = new Intent(ACTION_PAYLOAD_SENT);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    @Override
    public void onDeactivated(int reason) {
        // Connection lost or another AID selected — no state to clean up.
    }
}
