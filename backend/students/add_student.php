<?php
// Hajiri Book - Add Student API
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendJsonResponse("error", "Only POST method is allowed", null, 405);
}

$auth = AuthMiddleware::authenticate();
$input = json_decode(file_get_contents("php://input"), true);

$localUuid = trim($input['local_uuid'] ?? '');
$studentId = trim($input['student_id'] ?? '');
$fullName = trim($input['full_name'] ?? '');
$rollNumber = trim($input['roll_number'] ?? '');
$className = trim($input['class_name'] ?? '9th');
$gender = trim($input['gender'] ?? 'Male');
$dob = trim($input['dob'] ?? null);
$mobile = trim($input['mobile'] ?? null);

if (empty($fullName) || empty($rollNumber)) {
    sendJsonResponse("error", "Full name and roll number are required", null, 400);
}

$database = new Database();
$db = $database->getConnection();

// Find class_id
$classStmt = $db->prepare("SELECT id FROM classes WHERE class_name = :cname AND school_id = :sid LIMIT 1");
$classStmt->execute([':cname' => $className, ':sid' => $auth['school_id']]);
$classRow = $classStmt->fetch();
$classId = $classRow ? $classRow['id'] : 4; // default to 9th

if (empty($localUuid)) {
    $localUuid = uniqid('stu_', true);
}
if (empty($studentId)) {
    $studentId = 'STU-' . time() . '-' . rand(100, 999);
}

$insertStmt = $db->prepare("INSERT INTO students 
    (local_uuid, school_id, student_id, student_name, roll_number, class_id, dob, gender, mobile, status)
    VALUES (:uuid, :sid, :stuid, :sname, :roll, :cid, :dob, :gender, :mobile, 'ACTIVE')
    ON DUPLICATE KEY UPDATE 
    student_name = VALUES(student_name),
    roll_number = VALUES(roll_number),
    mobile = VALUES(mobile)");

$insertStmt->execute([
    ':uuid' => $localUuid,
    ':sid' => $auth['school_id'],
    ':stuid' => $studentId,
    ':sname' => $fullName,
    ':roll' => $rollNumber,
    ':cid' => $classId,
    ':dob' => $dob ?: null,
    ':gender' => $gender,
    ':mobile' => $mobile ?: null
]);

$remoteId = $db->lastInsertId();

sendJsonResponse("success", "Student registered successfully", [
    "id" => (int)$remoteId,
    "local_uuid" => $localUuid,
    "student_id" => $studentId,
    "full_name" => $fullName,
    "roll_number" => $rollNumber,
    "class_name" => $className
], 201);
?>
