<?php
// Hajiri Book - Mark Attendance API
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendJsonResponse("error", "Only POST method is allowed", null, 405);
}

$auth = AuthMiddleware::authenticate();
$input = json_decode(file_get_contents("php://input"), true);

$localUuid = trim($input['local_uuid'] ?? '');
$studentUuid = trim($input['student_uuid'] ?? '');
$date = trim($input['attendance_date'] ?? date('Y-m-d'));
$time = trim($input['attendance_time'] ?? date('H:i:s'));
$session = trim($input['session'] ?? 'MORNING');
$status = strtoupper(trim($input['attendance_status'] ?? 'PRESENT'));
$method = strtoupper(trim($input['attendance_method'] ?? 'FACE'));
$confidence = floatval($input['confidence'] ?? 1.0);
$deviceId = trim($input['device_id'] ?? 'ANDROID_DEVICE');

if (empty($studentUuid)) {
    sendJsonResponse("error", "student_uuid is required", null, 400);
}

$database = new Database();
$db = $database->getConnection();

// Lookup student id and class id
$studentStmt = $db->prepare("SELECT id, class_id, student_name FROM students WHERE local_uuid = :uuid AND school_id = :sid LIMIT 1");
$studentStmt->execute([':uuid' => $studentUuid, ':sid' => $auth['school_id']]);
$student = $studentStmt->fetch();

if (!$student) {
    sendJsonResponse("error", "Student not found with UUID: $studentUuid", null, 404);
}

$studentId = $student['id'];
$classId = $student['class_id'];

if (empty($localUuid)) {
    $localUuid = uniqid('att_', true);
}

try {
    $stmt = $db->prepare("INSERT INTO attendance 
        (local_uuid, school_id, student_id, class_id, attendance_date, attendance_time, attendance_session, attendance_status, attendance_method, confidence, device_id, teacher_id)
        VALUES (:luuid, :sid, :stuid, :cid, :adate, :atime, :asession, :astatus, :amethod, :conf, :dev, :tid)
        ON DUPLICATE KEY UPDATE
        attendance_time = VALUES(attendance_time),
        attendance_status = VALUES(attendance_status),
        attendance_method = VALUES(attendance_method),
        confidence = VALUES(confidence)");

    $stmt->execute([
        ':luuid' => $localUuid,
        ':sid' => $auth['school_id'],
        ':stuid' => $studentId,
        ':cid' => $classId,
        ':adate' => $date,
        ':atime' => $time,
        ':asession' => $session,
        ':astatus' => $status,
        ':amethod' => $method,
        ':conf' => $confidence,
        ':dev' => $deviceId,
        ':tid' => $auth['user_id']
    ]);

    $remoteId = $db->lastInsertId();

    sendJsonResponse("success", "Attendance recorded successfully", [
        "id" => (int)$remoteId,
        "local_uuid" => $localUuid,
        "student_name" => $student['student_name'],
        "attendance_status" => $status,
        "attendance_date" => $date,
        "attendance_time" => $time
    ], 200);

} catch (PDOException $e) {
    sendJsonResponse("error", "Failed to mark attendance: " . $e->getMessage(), null, 500);
}
?>
