package com.example.timska;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import com.example.timska.nfc.SecureToken;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class SecureTokenTest {

    @Test
    public void wrapThenUnwrapReturnsOriginalPayload() {
        byte[] payload = "{\"sid\":\"201234\"}".getBytes(StandardCharsets.UTF_8);

        byte[] recovered = SecureToken.unwrap(SecureToken.wrap(payload));

        assertArrayEquals(payload, recovered);
    }

    @Test
    public void tamperedTokenIsRejected() {
        byte[] token = SecureToken.wrap("payload".getBytes(StandardCharsets.UTF_8));
        // Flip the first character of the (base64) payload segment.
        token[0] = token[0] == 'A' ? (byte) 'B' : (byte) 'A';

        try {
            SecureToken.unwrap(token);
            fail("Expected SecurityException for a tampered token");
        } catch (SecurityException expected) {
            // good — signature no longer matches
        }
    }

    @Test
    public void malformedTokenIsRejected() {
        try {
            SecureToken.unwrap("not-a-valid-token".getBytes(StandardCharsets.UTF_8));
            fail("Expected SecurityException for a malformed token");
        } catch (SecurityException expected) {
            // good
        }
    }
}
