<?php
/**
 * POST /api/login.php
 * Body: { "username": "...", "password": "...", "role": "teacher|student|admin" }
 * Returns a signed JWT and the user profile on success.
 */
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Jwt.php';

Response::cors();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::error('Метод не е дозволен', 405);
}

$input = json_decode(file_get_contents('php://input'), true) ?? [];
$username = trim($input['username'] ?? '');
$password = $input['password'] ?? '';
$role     = trim($input['role'] ?? '');

if ($username === '' || $password === '' || $role === '') {
    Response::error('Потребни се корисничко име, лозинка и улога', 422);
}

$pdo = Database::connect();
$stmt = $pdo->prepare('SELECT * FROM users WHERE username = ? AND role = ? LIMIT 1');
$stmt->execute([$username, $role]);
$user = $stmt->fetch();

if (!$user || !password_verify($password, $user['password_hash'])) {
    Response::error('Погрешно корисничко име или лозинка', 401);
}

$token = Jwt::encode([
    'sub'  => (string) $user['id'],
    'role' => $user['role'],
    'name' => $user['full_name'],
    'iat'  => time(),
    'exp'  => time() + 60 * 60 * 8, // 8 hours
]);

Response::json([
    'success' => true,
    'token'   => $token,
    'user'    => [
        'id'         => (string) $user['id'],
        'full_name'  => $user['full_name'],
        'role'       => $user['role'],
        'student_id' => $user['student_id'] ?? '',
        'course'     => $user['course'] ?? '',
    ],
]);
