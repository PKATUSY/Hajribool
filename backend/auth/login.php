<?php
// Hajiri Book - Login Endpoint
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendJsonResponse("error", "Only POST method is allowed", null, 405);
}

$input = json_decode(file_get_contents("php://input"), true);
$username = trim($input['username'] ?? '');
$password = trim($input['password'] ?? '');
$requestedRole = trim($input['role'] ?? '');

if (empty($username) || empty($password)) {
    sendJsonResponse("error", "Username and password are required", null, 400);
}

$database = new Database();
$db = $database->getConnection();

$stmt = $db->prepare("SELECT u.*, s.school_name, s.school_code, s.address as school_address 
                      FROM users u 
                      JOIN schools s ON u.school_id = s.id 
                      WHERE u.username = :username AND u.status = 'ACTIVE' LIMIT 1");
$stmt->execute([':username' => $username]);
$user = $stmt->fetch();

if (!$user || !password_verify($password, $user['password_hash'])) {
    sendJsonResponse("error", "Invalid username or password", null, 401);
}

// Check role if specified
if (!empty($requestedRole) && strtoupper($user['role']) !== strtoupper($requestedRole)) {
    sendJsonResponse("error", "User is not authorized for the requested role '{$requestedRole}'", null, 403);
}

// Fetch assigned classes if teacher
$assignedClasses = "6th - 10th";
$teacherStmt = $db->prepare("SELECT assigned_classes FROM teachers WHERE user_id = :uid LIMIT 1");
$teacherStmt->execute([':uid' => $user['id']]);
$teacher = $teacherStmt->fetch();
if ($teacher && !empty($teacher['assigned_classes'])) {
    $assignedClasses = $teacher['assigned_classes'];
}

// Generate Auth Token
$token = AuthMiddleware::generateToken($user['id'], $user['username'], $user['role'], $user['school_id']);

$responseData = [
    "token" => $token,
    "user" => [
        "id" => (int)$user['id'],
        "username" => $user['username'],
        "full_name" => $user['full_name'],
        "role" => $user['role'],
        "mobile" => $user['mobile'],
        "email" => $user['email'],
        "school_id" => (int)$user['school_id'],
        "assigned_classes" => $assignedClasses
    ],
    "school" => [
        "id" => (int)$user['school_id'],
        "name" => $user['school_name'],
        "code" => $user['school_code'],
        "address" => $user['school_address'],
        "academic_session" => "2026-2027"
    ]
];

sendJsonResponse("success", "Login successful", $responseData, 200);
?>
