<?php
/**
 * GET /api/statistics.php  (JWT protected)
 * Aggregated figures for the dashboard. Teachers are scoped to their own data; admins see all.
 * Returns: totals (today / week / all), per-course counts, and a 7-day trend.
 */
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Jwt.php';

Response::cors();
$auth = Jwt::requirePayload();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    Response::error('Метод не е дозволен', 405);
}

$pdo = Database::connect();

// Role-based scope reused across every query below.
$scope = '';
$scopeParams = [];
if (($auth['role'] ?? '') === 'teacher') {
    $scope = ' AND teacher_id = ?';
    $scopeParams[] = (string) $auth['sub'];
}

$totalToday = scalar($pdo,
    "SELECT COUNT(*) FROM attendance WHERE DATE(timestamp) = CURDATE()$scope", $scopeParams);

$totalWeek = scalar($pdo,
    "SELECT COUNT(*) FROM attendance WHERE timestamp >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)$scope",
    $scopeParams);

$totalAll = scalar($pdo,
    "SELECT COUNT(*) FROM attendance WHERE 1=1$scope", $scopeParams);

$uniqueStudents = scalar($pdo,
    "SELECT COUNT(DISTINCT student_id) FROM attendance WHERE 1=1$scope", $scopeParams);

// Attendance per course (for the bar chart).
$perCourseStmt = $pdo->prepare(
    "SELECT COALESCE(NULLIF(course, ''), 'Непознато') AS course, COUNT(*) AS total
     FROM attendance WHERE 1=1$scope
     GROUP BY course ORDER BY total DESC"
);
$perCourseStmt->execute($scopeParams);
$perCourse = $perCourseStmt->fetchAll();

// Last 7 days trend (for the line chart).
$trendStmt = $pdo->prepare(
    "SELECT DATE(timestamp) AS day, COUNT(*) AS total
     FROM attendance
     WHERE timestamp >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)$scope
     GROUP BY DATE(timestamp) ORDER BY day ASC"
);
$trendStmt->execute($scopeParams);
$trend = $trendStmt->fetchAll();

Response::json([
    'success' => true,
    'totals'  => [
        'today'           => $totalToday,
        'week'            => $totalWeek,
        'all'             => $totalAll,
        'unique_students' => $uniqueStudents,
    ],
    'per_course' => $perCourse,
    'trend'      => $trend,
]);

function scalar(PDO $pdo, string $sql, array $params): int
{
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    return (int) $stmt->fetchColumn();
}
