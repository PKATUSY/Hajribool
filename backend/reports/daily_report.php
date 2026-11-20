<?php
// Hajiri Book - Daily Attendance Report API
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../middleware/auth.php';

$auth = AuthMiddleware::authenticate();
$date = trim($_GET['date'] ?? date('Y-m-d'));
$classId = isset($_GET['class_id']) ? (int)$_GET['class_id'] : null;

$database = new Database();
$db = $database->getConnection();

$query = "SELECT 
            c.class_name,
            COUNT(s.id) as total_students,
            SUM(CASE WHEN a.attendance_status = 'PRESENT' THEN 1 ELSE 0 END) as present_count,
            SUM(CASE WHEN a.attendance_status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count,
            SUM(CASE WHEN a.attendance_status = 'LATE' THEN 1 ELSE 0 END) as late_count
          FROM students s
          JOIN classes c ON s.class_id = c.id
          LEFT JOIN attendance a ON s.id = a.student_id AND a.attendance_date = :adate
          WHERE s.school_id = :sid";

if ($classId) {
    $query .= " AND s.class_id = :cid";
}
$query .= " GROUP BY c.id ORDER BY c.numeric_order ASC";

$stmt = $db->prepare($query);
$params = [':adate' => $date, ':sid' => $auth['school_id']];
if ($classId) {
    $params[':cid'] = $classId;
}
$stmt->execute($params);
$classReports = $stmt->fetchAll();

$totalAll = 0;
$presentAll = 0;
$absentAll = 0;

foreach ($classReports as $cr) {
    $totalAll += (int)$cr['total_students'];
    $presentAll += (int)$cr['present_count'];
    $absentAll += (int)$cr['absent_count'];
}

$percentageAll = $totalAll > 0 ? round(($presentAll / $totalAll) * 100, 1) : 0;

sendJsonResponse("success", "Daily report generated", [
    "date" => $date,
    "summary" => [
        "total" => $totalAll,
        "present" => $presentAll,
        "absent" => $absentAll,
        "percentage" => $percentageAll
    ],
    "classes" => $classReports
], 200);
?>
