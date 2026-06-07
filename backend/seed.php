<?php
/**
 * One-off seed script. Open http://localhost/cps/seed.php once after importing schema.sql.
 * Creates demo users (with properly hashed passwords) and some sample attendance so the
 * dashboard charts have data to show immediately. Safe to run repeatedly (idempotent).
 *
 * Demo credentials (password in brackets):
 *   admin   / admin123   (role: admin)    — dashboard, sees everything
 *   prof    / test123    (role: teacher)  — teacher app + dashboard, sees own classes
 *   student / test123    (role: student)  — student app
 */
require_once __DIR__ . '/config/Database.php';

header('Content-Type: text/plain; charset=utf-8');
$pdo = Database::connect();

// --- Users ---
$users = [
    ['admin',   'admin123', 'admin',   'Администратор',          null,     null],
    ['prof',    'test123',  'teacher', 'Проф. Марко Јованоски',  null,     'Мобилни апликации'],
    ['student', 'test123',  'student', 'Ана Стоилкова',          '201234', 'Мобилни апликации'],
];

$insertUser = $pdo->prepare(
    'INSERT INTO users (username, password_hash, role, full_name, student_id, course)
     VALUES (?, ?, ?, ?, ?, ?)
     ON DUPLICATE KEY UPDATE
        password_hash = VALUES(password_hash),
        full_name     = VALUES(full_name),
        student_id    = VALUES(student_id),
        course        = VALUES(course)'
);
foreach ($users as [$username, $password, $role, $fullName, $studentId, $course]) {
    $insertUser->execute([
        $username, password_hash($password, PASSWORD_DEFAULT), $role, $fullName, $studentId, $course,
    ]);
}
echo "Seeded " . count($users) . " users.\n";

// teacher_id used by sample attendance = the prof's user id
$teacherId = (string) $pdo->query("SELECT id FROM users WHERE username='prof' AND role='teacher'")
    ->fetchColumn();

// --- Courses ---
$pdo->prepare(
    'INSERT IGNORE INTO courses (name, teacher_id) VALUES (?, ?), (?, ?), (?, ?)'
)->execute([
    'Мобилни апликации', $teacherId,
    'Веб програмирање',  $teacherId,
    'Бази на податоци',  $teacherId,
]);

// --- Sample attendance spread across the last 7 days and a few courses ---
$sampleStudents = [
    ['201234', 'Ана Стоилкова',     'Мобилни апликации'],
    ['201235', 'Бојан Петров',      'Мобилни апликации'],
    ['201236', 'Викторија Илиева',  'Веб програмирање'],
    ['201237', 'Дарко Николов',     'Бази на податоци'],
    ['201238', 'Елена Трајкова',    'Мобилни апликации'],
    ['201239', 'Филип Стојанов',    'Веб програмирање'],
];

$insertAtt = $pdo->prepare(
    'INSERT IGNORE INTO attendance
        (student_id, student_name, course, class_name, teacher_id, timestamp)
     VALUES (?, ?, ?, ?, ?, ?)'
);

$added = 0;
for ($dayOffset = 6; $dayOffset >= 0; $dayOffset--) {
    // a random-ish subset attends each day
    foreach ($sampleStudents as $i => [$sid, $name, $course]) {
        if (($i + $dayOffset) % 3 === 0) {
            continue; // skip some to make the chart look natural
        }
        $ts = date('Y-m-d', strtotime("-$dayOffset day")) . ' '
            . sprintf('%02d:%02d:00', 9 + ($i % 4), ($i * 7) % 60);
        $insertAtt->execute([$sid, $name, $course, $course, $teacherId, $ts]);
        $added += $insertAtt->rowCount();
    }
}
echo "Seeded $added sample attendance records.\n";
echo "Done. You can delete this file after seeding.\n";
