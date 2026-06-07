package com.example.timska.nfc;

import java.io.ByteArrayOutputStream;

/**
 * Shared APDU protocol used by both sides of the NFC tap.
 *
 * The student device runs Host-based Card Emulation (HCE) and registers our AID. When the teacher
 * device (in reader mode) sends a SELECT-AID command, Android routes it to {@link StudentApduService},
 * which answers with the student payload followed by the {@code 90 00} success status word.
 */
public final class NfcProtocol {

    private NfcProtocol() {
    }

    /** Proprietary AID (F0...) registered in res/xml/apduservice.xml. */
    public static final byte[] AID = {
            (byte) 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06
    };

    /** Status word: success. */
    public static final byte[] SW_OK = {(byte) 0x90, 0x00};
    /** Status word: generic failure. */
    public static final byte[] SW_ERROR = {(byte) 0x6F, 0x00};

    private static final byte CLA_ISO = 0x00;
    private static final byte INS_SELECT = (byte) 0xA4;
    private static final byte P1_SELECT_BY_NAME = 0x04;

    /** Builds the SELECT-by-AID command APDU that the reader sends first. */
    public static byte[] buildSelectApdu() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(CLA_ISO);
        out.write(INS_SELECT);
        out.write(P1_SELECT_BY_NAME);
        out.write(0x00);            // P2
        out.write(AID.length);      // Lc
        out.write(AID, 0, AID.length);
        out.write(0x00);            // Le
        return out.toByteArray();
    }

    /** True if the incoming APDU is a SELECT-by-name command targeting our AID. */
    public static boolean isSelectAidApdu(byte[] apdu) {
        if (apdu == null || apdu.length < 5 + AID.length) {
            return false;
        }
        if (apdu[0] != CLA_ISO || apdu[1] != INS_SELECT || apdu[2] != P1_SELECT_BY_NAME) {
            return false;
        }
        int lc = apdu[4] & 0xFF;
        if (lc != AID.length || apdu.length < 5 + AID.length) {
            return false;
        }
        for (int i = 0; i < AID.length; i++) {
            if (apdu[5 + i] != AID[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /** True if the response ends with the {@code 90 00} success status word. */
    public static boolean endsWithOk(byte[] response) {
        int n = response.length;
        return n >= 2 && response[n - 2] == (byte) 0x90 && response[n - 1] == 0x00;
    }
}
