<?php
require_once __DIR__ . '/config/cors.php';

sendJsonResponse("success", "Hajiri Book REST API v1.0.0 is operational", [
    "service" => "Hajiri Book API",
    "version" => "1.0.0",
    "timestamp" => date("Y-m-d H:i:s"),
    "endpoints" => [
        "POST /api/auth/login.php",
        "GET /api/students/get_students.php",
        "POST /api/students/add_student.php",
        "POST /api/face/register_face.php",
        "POST /api/attendance/mark_attendance.php",
        "POST /api/sync/sync_push.php",
        "GET /api/sync/sync_pull.php",
        "GET /api/reports/daily_report.php"
    ]
], 200);
?>
