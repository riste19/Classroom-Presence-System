package com.example.timska;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.timska.nfc.NfcProtocol;

import org.junit.Test;

import java.util.Arrays;

public class NfcProtocolTest {

    @Test
    public void selectApduHasCorrectHeaderAndAid() {
        byte[] apdu = NfcProtocol.buildSelectApdu();

        // 00 A4 04 00 | Lc | AID... | Le
        assertEquals((byte) 0x00, apdu[0]);
        assertEquals((byte) 0xA4, apdu[1]);
        assertEquals((byte) 0x04, apdu[2]);
        assertEquals((byte) 0x00, apdu[3]);
        assertEquals(NfcProtocol.AID.length, apdu[4] & 0xFF);

        byte[] embeddedAid = Arrays.copyOfRange(apdu, 5, 5 + NfcProtocol.AID.length);
        assertArrayEquals(NfcProtocol.AID, embeddedAid);
    }

    @Test
    public void isSelectAidApduRecognisesOwnCommand() {
        assertTrue(NfcProtocol.isSelectAidApdu(NfcProtocol.buildSelectApdu()));
    }

    @Test
    public void isSelectAidApduRejectsForeignCommand() {
        byte[] other = {0x00, (byte) 0xA4, 0x04, 0x00, 0x02, 0x11, 0x22, 0x00};
        assertFalse(NfcProtocol.isSelectAidApdu(other));
        assertFalse(NfcProtocol.isSelectAidApdu(new byte[]{0x00, 0x01}));
        assertFalse(NfcProtocol.isSelectAidApdu(null));
    }

    @Test
    public void endsWithOkDetectsStatusWord() {
        assertTrue(NfcProtocol.endsWithOk(new byte[]{0x10, (byte) 0x90, 0x00}));
        assertFalse(NfcProtocol.endsWithOk(new byte[]{0x10, 0x6F, 0x00}));
    }
}
