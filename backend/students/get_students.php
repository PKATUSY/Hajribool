<?php
// Hajiri Book - Get Students API
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

$auth = AuthMiddleware::authenticate();
$schoolId = (int)($_GET['school_id'] ?? $auth['school_id']);
$since = (int)($_GET['since'] ?? 0);

$database = new Database();
$db = $database->getConnection();

$query = "SELECT s.*, c.class_name, sec.section_name 
          FROM students s
          JOIN classes c ON s.class_id = c.id
          LEFT JOIN sections sec ON s.section_id = sec.id
          WHERE s.school_id = :school_id";

if ($since > 0) {
    $query .= " AND UNIX_TIMESTAMP(s.updated_at) >= :since";
}
$query .= " ORDER BY c.numeric_order ASC, CAST(s.roll_number AS UNSIGNED) ASC";

$stmt = $db->prepare($query);
$params = [':school_id' => $schoolId];
if ($since > 0) {
    $params[':since'] = $since;
}
$stmt->execute($params);
$rows = $stmt->fetchAll();

$students = [];
foreach ($rows as $row) {
    $students[] = [
        "id" => (int)$row['id'],
        "local_uuid" => $row['local_uuid'],
        "student_id" => $row['student_id'],
        "admission_no" => $row['admission_no'],
        "full_name" => $row['student_name'],
        "father_name" => $row['father_name'],
        "mother_name" => $row['mother_name'],
        "roll_number" => $row['roll_number'],
        "class_name" => $row['class_name'],
        "section" => $row['section_name'] ?? 'A',
        "dob" => $row['dob'],
        "gender" => $row['gender'],
        "mobile" => $row['mobile'],
        "has_face_enrolled" => (bool)$row['has_face_enrolled'],
        "updated_at" => strtotime($row['updated_at'])
    ];
}

sendJsonResponse("success", "Students fetched successfully", $students, 200);
?>
