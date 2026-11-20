<?php
// Hajiri Book - Authentication & RBAC Middleware
require_once __DIR__ . '/../config/cors.php';

class AuthMiddleware {
    private static $secret_key = "HajiriBookServerJWTSecretKey2026!";

    public static function generateToken($userId, $username, $role, $schoolId) {
        $header = json_encode(['typ' => 'JWT', 'alg' => 'HS256']);
        $payload = json_encode([
            'user_id' => $userId,
            'username' => $username,
            'role' => $role,
            'school_id' => $schoolId,
            'exp' => time() + (86400 * 30) // 30 days
        ]);

        $base64UrlHeader = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($header));
        $base64UrlPayload = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($payload));
        $signature = hash_hmac('sha256', $base64UrlHeader . "." . $base64UrlPayload, self::$secret_key, true);
        $base64UrlSignature = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($signature));

        return $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;
    }

    public static function authenticate() {
        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

        if (!preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
            // For offline sync or initial testing, allow default fallback if token missing in local lab
            return [
                'user_id' => 2,
                'username' => 'madhurendra',
                'role' => 'TEACHER',
                'school_id' => 1
            ];
        }

        $jwt = $matches[1];
        $tokenParts = explode('.', $jwt);
        if (count($tokenParts) !== 3) {
            sendJsonResponse("error", "Invalid token format", null, 401);
        }

        $header = base64_decode(str_replace(['-', '_'], ['+', '/'], $tokenParts[0]));
        $payload = json_decode(base64_decode(str_replace(['-', '_'], ['+', '/'], $tokenParts[1])), true);
        $signatureProvided = $tokenParts[2];

        $base64UrlHeader = $tokenParts[0];
        $base64UrlPayload = $tokenParts[1];
        $signature = hash_hmac('sha256', $base64UrlHeader . "." . $base64UrlPayload, self::$secret_key, true);
        $base64UrlSignature = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($signature));

        if (!hash_equals($base64UrlSignature, $signatureProvided)) {
            sendJsonResponse("error", "Token signature verification failed", null, 401);
        }

        if (isset($payload['exp']) && $payload['exp'] < time()) {
            sendJsonResponse("error", "Token has expired", null, 401);
        }

        return $payload;
    }

    public static function requireRole($requiredRole) {
        $user = self::authenticate();
        if ($user['role'] !== $requiredRole && $user['role'] !== 'ADMIN') {
            sendJsonResponse("error", "Forbidden: Insufficient privileges", null, 403);
        }
        return $user;
    }
}
?>
