package com.example.timska.nfc;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Wraps the NFC payload in a short-lived, HMAC-signed token so the teacher device broadcasts a
 * <em>dynamic, verifiable</em> value instead of a static plaintext student ID.
 *
 * Token format (UTF-8): {@code base64url(payload) "." issuedAtMillis "." base64url(hmacSha256)}.
 * The teacher verifies the signature and rejects tokens older than {@link #VALIDITY_MS}, which
 * mitigates replay/cloning of a captured payload.
 *
 * The HMAC secret is shared by both roles (same app). In production it would be provisioned per
 * session by the server rather than compiled in.
 */
public final class SecureToken {

    private SecureToken() {
    }

    private static final String SECRET = "cps_nfc_shared_secret_2026";
    private static final String HMAC_ALG = "HmacSHA256";

    /** Accepted clock window between the two phones (also bounds replay). */
    public static final long VALIDITY_MS = 5 * 60 * 1000L;

    /** Signs {@code payload} and returns the token bytes to broadcast over NFC. */
    public static byte[] wrap(byte[] payload) {
        String body = base64(payload) + "." + System.currentTimeMillis();
        String signature = base64(hmac(body));
        return (body + "." + signature).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Verifies the token (signature + freshness) and returns the original payload bytes.
     *
     * @throws SecurityException if the signature is invalid or the token has expired
     */
    public static byte[] unwrap(byte[] tokenBytes) {
        String token = new String(tokenBytes, StandardCharsets.UTF_8);
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new SecurityException("Невалиден формат на токен");
        }
        String body = parts[0] + "." + parts[1];
        if (!constantTimeEquals(base64(hmac(body)), parts[2])) {
            throw new SecurityException("Потписот не се совпаѓа");
        }
        long issuedAt;
        try {
            issuedAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new SecurityException("Невалиден временски печат");
        }
        if (Math.abs(System.currentTimeMillis() - issuedAt) > VALIDITY_MS) {
            throw new SecurityException("Токенот е истечен");
        }
        return Base64.getUrlDecoder().decode(parts[0]);
    }

    private static byte[] hmac(String message) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC недостапен", e);
        }
    }

    private static String base64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
