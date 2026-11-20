<?php
// Hajiri Book - Bidirectional Sync Pull API (Server -> Android)
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

$auth = AuthMiddleware::authenticate();
$schoolId = (int)($_GET['school_id'] ?? $auth['school_id']);
$since = (int)($_GET['since'] ?? 0);

$database = new Database();
$db = $database->getConnection();

// 1. Pull Students
$studentQuery = "SELECT s.*, c.class_name, sec.section_name 
                 FROM students s
                 JOIN classes c ON s.class_id = c.id
                 LEFT JOIN sections sec ON s.section_id = sec.id
                 WHERE s.school_id = :sid";
if ($since > 0) {
    $studentQuery .= " AND UNIX_TIMESTAMP(s.updated_at) >= :since";
}
$sStmt = $db->prepare($studentQuery);
$params = [':sid' => $schoolId];
if ($since > 0) $params[':since'] = $since;
$sStmt->execute($params);

$students = [];
while ($row = $sStmt->fetch()) {
    $students[] = [
        "id" => (int)$row['id'],
        "local_uuid" => $row['local_uuid'],
        "student_id" => $row['student_id'],
        "full_name" => $row['student_name'],
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

// 2. Pull Face Templates
$templateQuery = "SELECT ft.* 
                  FROM student_face_templates ft 
                  JOIN students s ON ft.student_id = s.id 
                  WHERE s.school_id = :sid";
$tStmt = $db->prepare($templateQuery);
$tStmt->execute([':sid' => $schoolId]);
$faceTemplates = [];
while ($tRow = $tStmt->fetch()) {
    $faceTemplates[] = [
        "student_uuid" => $tRow['student_uuid'],
        "sample_angle" => $tRow['sample_angle'],
        "embedding" => json_decode($tRow['embedding_json'], true) ?: [],
        "quality_score" => floatval($tRow['quality_score'])
    ];
}

// 3. Pull Recent Attendance
$attQuery = "SELECT a.*, s.student_name, s.roll_number, c.class_name 
             FROM attendance a 
             JOIN students s ON a.student_id = s.id 
             JOIN classes c ON a.class_id = c.id 
             WHERE a.school_id = :sid AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
$aStmt = $db->prepare($attQuery);
$aStmt->execute([':sid' => $schoolId]);
$attendanceList = [];
while ($aRow = $aStmt->fetch()) {
    $attendanceList[] = [
        "id" => (int)$aRow['id'],
        "local_uuid" => $aRow['local_uuid'],
        "student_uuid" => $aRow['student_uuid'] ?? '',
        "student_name" => $aRow['student_name'],
        "roll_number" => $aRow['roll_number'],
        "class_name" => $aRow['class_name'],
        "section" => "A",
        "attendance_date" => $aRow['attendance_date'],
        "attendance_time" => $aRow['attendance_time'],
        "attendance_status" => $aRow['attendance_status'],
        "attendance_method" => $aRow['attendance_method'],
        "confidence" => floatval($aRow['confidence'])
    ];
}

sendJsonResponse("success", "Sync pull completed successfully", [
    "students" => $students,
    "face_templates" => $faceTemplates,
    "attendance" => $attendanceList,
    "server_timestamp" => time()
], 200);
?>
