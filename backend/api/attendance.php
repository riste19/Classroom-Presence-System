<?php
/**
 * /api/attendance.php  (JWT protected)
 *
 *   POST — bulk upload from the teacher app.
 *          Body: { "records": [ { student_id, student_name, course, class_name, teacher_id, timestamp } ] }
 *          Duplicate (student + teacher + day) records are ignored.
 *
 *   GET  — list records for the dashboard, with optional filters:
 *          ?date=YYYY-MM-DD&course=...&student=...&teacher=...&limit=100&offset=0
 *          Teachers only ever see their own records; admins see everything.
 */
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Jwt.php';

Response::cors();
$auth = Jwt::requirePayload();
$pdo = Database::connect();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    handleUpload($pdo);
} elseif ($_SERVER['REQUEST_METHOD'] === 'GET') {
    handleList($pdo, $auth);
} else {
    Response::error('Метод не е дозволен', 405);
}

function handleUpload(PDO $pdo): void
{
    $input = json_decode(file_get_contents('php://input'), true) ?? [];
    $records = $input['records'] ?? null;
    if (!is_array($records)) {
        Response::error('Очекувано поле "records" со низа од записи', 422);
    }

    // INSERT IGNORE relies on the UNIQUE(student_id, teacher_id, attend_date) key to dedupe.
    $sql = 'INSERT IGNORE INTO attendance
                (student_id, student_name, course, class_name, teacher_id, timestamp)
            VALUES (?, ?, ?, ?, ?, ?)';
    $stmt = $pdo->prepare($sql);

    $inserted = 0;
    $pdo->beginTransaction();
    try {
        foreach ($records as $r) {
            $stmt->execute([
                trim($r['student_id'] ?? ''),
                trim($r['student_name'] ?? ''),
                $r['course'] ?? null,
                $r['class_name'] ?? null,
                trim($r['teacher_id'] ?? ''),
                $r['timestamp'] ?? date('Y-m-d H:i:s'),
            ]);
            $inserted += $stmt->rowCount();
        }
        $pdo->commit();
    } catch (PDOException $e) {
        $pdo->rollBack();
        Response::error('Запишувањето не успеа: ' . $e->getMessage(), 500);
    }

    Response::json([
        'success'  => true,
        'received' => count($records),
        'inserted' => $inserted,
    ]);
}

function handleList(PDO $pdo, array $auth): void
{
    $where = [];
    $params = [];

    // Role-based access control: teachers are scoped to their own records.
    if (($auth['role'] ?? '') === 'teacher') {
        $where[] = 'teacher_id = ?';
        $params[] = (string) $auth['sub'];
    } elseif (!empty($_GET['teacher'])) {
        $where[] = 'teacher_id = ?';
        $params[] = $_GET['teacher'];
    }

    if (!empty($_GET['date'])) {
        $where[] = 'DATE(timestamp) = ?';
        $params[] = $_GET['date'];
    }
    if (!empty($_GET['course'])) {
        $where[] = 'course = ?';
        $params[] = $_GET['course'];
    }
    if (!empty($_GET['student'])) {
        $where[] = '(student_id LIKE ? OR student_name LIKE ?)';
        $params[] = '%' . $_GET['student'] . '%';
        $params[] = '%' . $_GET['student'] . '%';
    }

    $limit  = max(1, min(500, (int) ($_GET['limit'] ?? 100)));
    $offset = max(0, (int) ($_GET['offset'] ?? 0));

    $sql = 'SELECT id, student_id, student_name, course, class_name, teacher_id, timestamp
            FROM attendance';
    if ($where) {
        $sql .= ' WHERE ' . implode(' AND ', $where);
    }
    $sql .= ' ORDER BY timestamp DESC LIMIT ' . $limit . ' OFFSET ' . $offset;

    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $records = $stmt->fetchAll();

    Response::json([
        'success' => true,
        'count'   => count($records),
        'limit'   => $limit,
        'offset'  => $offset,
        'records' => $records,
    ]);
}
