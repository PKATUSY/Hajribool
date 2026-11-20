<?php
// Hajiri Book - Register Face Template API
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendJsonResponse("error", "Only POST method is allowed", null, 405);
}

$auth = AuthMiddleware::authenticate();
$input = json_decode(file_get_contents("php://input"), true);

$studentUuid = trim($input['student_uuid'] ?? '');
$sampleAngle = trim($input['sample_angle'] ?? 'FRONT');
$embedding = $input['embedding'] ?? [];
$qualityScore = floatval($input['quality_score'] ?? 1.0);

if (empty($studentUuid) || empty($embedding) || !is_array($embedding)) {
    sendJsonResponse("error", "Student UUID and valid embedding vector are required", null, 400);
}

$database = new Database();
$db = $database->getConnection();

// Lookup student id by uuid
$studentStmt = $db->prepare("SELECT id FROM students WHERE local_uuid = :uuid AND school_id = :sid LIMIT 1");
$studentStmt->execute([':uuid' => $studentUuid, ':sid' => $auth['school_id']]);
$student = $studentStmt->fetch();

if (!$student) {
    sendJsonResponse("error", "Student not found with provided UUID", null, 404);
}

$studentId = $student['id'];
$embeddingJson = json_encode($embedding);

$stmt = $db->prepare("INSERT INTO student_face_templates 
    (student_id, student_uuid, sample_angle, embedding_json, quality_score)
    VALUES (:sid, :suuid, :angle, :emb, :quality)");

$stmt->execute([
    ':sid' => $studentId,
    ':suuid' => $studentUuid,
    ':angle' => $sampleAngle,
    ':emb' => $embeddingJson,
    ':quality' => $qualityScore
]);

// Update student enrolled flag
$updateStmt = $db->prepare("UPDATE students SET has_face_enrolled = 1 WHERE id = :id");
$updateStmt->execute([':id' => $studentId]);

sendJsonResponse("success", "Face template registered successfully", [
    "template_id" => (int)$db->lastInsertId(),
    "student_uuid" => $studentUuid
], 201);
?>
