<?php
// Hajiri Book - Bidirectional Sync Push API (Android -> Server)
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendJsonResponse("error", "Only POST method is allowed", null, 405);
}

$auth = AuthMiddleware::authenticate();
$input = json_decode(file_get_contents("php://input"), true);

$students = $input['students'] ?? [];
$attendance = $input['attendance'] ?? [];
$faceTemplates = $input['face_templates'] ?? [];

$database = new Database();
$db = $database->getConnection();

$syncedStudents = [];
$syncedAttendance = [];

try {
    $db->beginTransaction();

    // 1. Process Batch Students
    $studentStmt = $db->prepare("INSERT INTO students 
        (local_uuid, school_id, student_id, student_name, roll_number, class_id, dob, gender, mobile, has_face_enrolled, status)
        VALUES (:uuid, :sid, :stuid, :sname, :roll, :cid, :dob, :gender, :mobile, :enrolled, 'ACTIVE')
        ON DUPLICATE KEY UPDATE 
        student_name = VALUES(student_name),
        roll_number = VALUES(roll_number),
        has_face_enrolled = VALUES(has_face_enrolled)");

    foreach ($students as $s) {
        $localUuid = $s['local_uuid'] ?? '';
        if (empty($localUuid)) continue;

        // Find class
        $className = $s['class_name'] ?? '9th';
        $classStmt = $db->prepare("SELECT id FROM classes WHERE class_name = :cname AND school_id = :sid LIMIT 1");
        $classStmt->execute([':cname' => $className, ':sid' => $auth['school_id']]);
        $cRow = $classStmt->fetch();
        $classId = $cRow ? $cRow['id'] : 4;

        $studentStmt->execute([
            ':uuid' => $localUuid,
            ':sid' => $auth['school_id'],
            ':stuid' => $s['student_id'] ?? ('STU-' . time()),
            ':sname' => $s['full_name'] ?? 'Student',
            ':roll' => $s['roll_number'] ?? '1',
            ':cid' => $classId,
            ':dob' => $s['dob'] ?? null,
            ':gender' => $s['gender'] ?? 'Male',
            ':mobile' => $s['mobile'] ?? null,
            ':enrolled' => !empty($s['has_face_enrolled']) ? 1 : 0
        ]);

        // Get ID
        $getIdStmt = $db->prepare("SELECT id FROM students WHERE local_uuid = :uuid LIMIT 1");
        $getIdStmt->execute([':uuid' => $localUuid]);
        $idRow = $getIdStmt->fetch();
        if ($idRow) {
            $syncedStudents[$localUuid] = (int)$idRow['id'];
        }
    }

    // 2. Process Batch Attendance
    $attStmt = $db->prepare("INSERT INTO attendance 
        (local_uuid, school_id, student_id, class_id, attendance_date, attendance_time, attendance_session, attendance_status, attendance_method, confidence, device_id, teacher_id)
        VALUES (:luuid, :sid, :stuid, :cid, :adate, :atime, :asession, :astatus, :amethod, :conf, :dev, :tid)
        ON DUPLICATE KEY UPDATE
        attendance_status = VALUES(attendance_status),
        attendance_time = VALUES(attendance_time)");

    foreach ($attendance as $a) {
        $localUuid = $a['local_uuid'] ?? '';
        $studentUuid = $a['student_uuid'] ?? '';
        if (empty($localUuid) || empty($studentUuid)) continue;

        // Resolve student ID
        $sLookup = $db->prepare("SELECT id, class_id FROM students WHERE local_uuid = :uuid LIMIT 1");
        $sLookup->execute([':uuid' => $studentUuid]);
        $studentRow = $sLookup->fetch();
        if (!$studentRow) continue;

        $attStmt->execute([
            ':luuid' => $localUuid,
            ':sid' => $auth['school_id'],
            ':stuid' => $studentRow['id'],
            ':cid' => $studentRow['class_id'],
            ':adate' => $a['attendance_date'] ?? date('Y-m-d'),
            ':atime' => $a['attendance_time'] ?? date('H:i:s'),
            ':asession' => $a['session'] ?? 'MORNING',
            ':astatus' => $a['attendance_status'] ?? 'PRESENT',
            ':amethod' => $a['attendance_method'] ?? 'FACE',
            ':conf' => floatval($a['confidence'] ?? 1.0),
            ':dev' => $a['device_id'] ?? 'ANDROID_DEVICE',
            ':tid' => $auth['user_id']
        ]);

        $getAttId = $db->prepare("SELECT id FROM attendance WHERE local_uuid = :uuid LIMIT 1");
        $getAttId->execute([':uuid' => $localUuid]);
        $attRow = $getAttId->fetch();
        if ($attRow) {
            $syncedAttendance[$localUuid] = (int)$attRow['id'];
        }
    }

    $db->commit();

    sendJsonResponse("success", "Sync push completed successfully", [
        "synced_students" => $syncedStudents,
        "synced_attendance" => $syncedAttendance,
        "server_timestamp" => time()
    ], 200);

} catch (Exception $e) {
    $db->rollBack();
    sendJsonResponse("error", "Sync push transaction failed: " . $e->getMessage(), null, 500);
}
?>
