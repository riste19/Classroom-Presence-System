<?php
/**
 * Minimal self-contained JWT (HS256) implementation — no external dependencies needed.
 * Tokens are signed with HMAC-SHA256 so the server can verify them without a session store.
 */
require_once __DIR__ . '/Response.php';

class Jwt
{
    // In a real deployment this would live outside the codebase (env var / secret manager).
    private const SECRET = 'cps_super_secret_change_me_2026';

    public static function encode(array $payload): string
    {
        $header = ['alg' => 'HS256', 'typ' => 'JWT'];
        $segments = [
            self::base64UrlEncode(json_encode($header)),
            self::base64UrlEncode(json_encode($payload)),
        ];
        $signingInput = implode('.', $segments);
        $signature = hash_hmac('sha256', $signingInput, self::SECRET, true);
        $segments[] = self::base64UrlEncode($signature);
        return implode('.', $segments);
    }

    /** Returns the decoded payload, or null if the token is invalid/expired. */
    public static function decode(string $token): ?array
    {
        $parts = explode('.', $token);
        if (count($parts) !== 3) {
            return null;
        }
        [$headerB64, $payloadB64, $signatureB64] = $parts;
        $expected = self::base64UrlEncode(
            hash_hmac('sha256', $headerB64 . '.' . $payloadB64, self::SECRET, true)
        );
        if (!hash_equals($expected, $signatureB64)) {
            return null;
        }
        $payload = json_decode(self::base64UrlDecode($payloadB64), true);
        if (!is_array($payload)) {
            return null;
        }
        if (isset($payload['exp']) && time() >= $payload['exp']) {
            return null;
        }
        return $payload;
    }

    /** Extracts and verifies the Bearer token; sends 401 and exits if missing/invalid. */
    public static function requirePayload(): array
    {
        $header = self::authorizationHeader();
        if (!$header || stripos($header, 'Bearer ') !== 0) {
            Response::error('Недостасува токен за авторизација', 401);
        }
        $token = trim(substr($header, 7));
        $payload = self::decode($token);
        if ($payload === null) {
            Response::error('Невалиден или истечен токен', 401);
        }
        return $payload;
    }

    private static function authorizationHeader(): ?string
    {
        if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
            return $_SERVER['HTTP_AUTHORIZATION'];
        }
        if (function_exists('getallheaders')) {
            foreach (getallheaders() as $name => $value) {
                if (strcasecmp($name, 'Authorization') === 0) {
                    return $value;
                }
            }
        }
        return $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? null;
    }

    private static function base64UrlEncode(string $data): string
    {
        return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
    }

    private static function base64UrlDecode(string $data): string
    {
        return base64_decode(strtr($data, '-_', '+/'));
    }
}
