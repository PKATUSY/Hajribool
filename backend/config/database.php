<?php
// Hajiri Book - Database Configuration
class Database {
    private $host = "localhost";
    private $db_name = "hajiri_book";
    private $username = "hajiri_user";
    private $password = "HajiriPass2026!";
    private $conn;

    public function getConnection() {
        $this->conn = null;
        try {
            $dsn = "mysql:host=" . $this->host . ";dbname=" . $this->db_name . ";charset=utf8mb4";
            $options = [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
            ];
            $this->conn = new PDO($dsn, $this->username, $this->password, $options);
        } catch (PDOException $exception) {
            http_response_code(500);
            echo json_encode([
                "status" => "error",
                "message" => "Database connection failure: " . $exception->getMessage()
            ]);
            exit;
        }
        return $this->conn;
    }
}
?>
