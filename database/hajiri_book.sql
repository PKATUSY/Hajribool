-- ========================================================
-- HAJIRI BOOK - SMART ATTENDANCE SYSTEM
-- Complete Production MySQL Database Schema
-- Version: 1.0.0
-- ========================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `sync_logs`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `attendance`;
DROP TABLE IF EXISTS `student_face_templates`;
DROP TABLE IF EXISTS `students`;
DROP TABLE IF EXISTS `sections`;
DROP TABLE IF EXISTS `classes`;
DROP TABLE IF EXISTS `teachers`;
DROP TABLE IF EXISTS `attendance_sessions`;
DROP TABLE IF EXISTS `academic_sessions`;
DROP TABLE IF EXISTS `devices`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `schools`;
SET FOREIGN_KEY_CHECKS = 1;

-- --------------------------------------------------------
-- 1. Schools Table
-- --------------------------------------------------------
CREATE TABLE `schools` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `school_code` VARCHAR(50) NOT NULL UNIQUE,
  `school_name` VARCHAR(255) NOT NULL,
  `address` TEXT NULL,
  `city` VARCHAR(100) NULL,
  `state` VARCHAR(100) NULL,
  `pincode` VARCHAR(20) NULL,
  `contact_number` VARCHAR(20) NULL,
  `email` VARCHAR(100) NULL,
  `logo_url` VARCHAR(255) NULL,
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 2. Academic Sessions Table
-- --------------------------------------------------------
CREATE TABLE `academic_sessions` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `school_id` BIGINT UNSIGNED NOT NULL,
  `session_name` VARCHAR(50) NOT NULL, -- e.g. "2026-2027"
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `is_current` TINYINT(1) DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 3. Users Table (Admin, Teachers, Staff)
-- --------------------------------------------------------
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `school_id` BIGINT UNSIGNED NOT NULL,
  `username` VARCHAR(100) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(150) NOT NULL,
  `role` ENUM('ADMIN', 'TEACHER', 'STAFF') NOT NULL DEFAULT 'TEACHER',
  `mobile` VARCHAR(20) NOT NULL,
  `email` VARCHAR(100) NULL,
  `profile_photo` VARCHAR(255) NULL,
  `status` ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 4. Teachers Table (Extended Teacher Profile)
-- --------------------------------------------------------
CREATE TABLE `teachers` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL UNIQUE,
  `employee_id` VARCHAR(50) NOT NULL UNIQUE,
  `qualification` VARCHAR(150) NULL,
  `designation` VARCHAR(100) DEFAULT 'Teacher',
  `assigned_classes` VARCHAR(255) DEFAULT '6th - 10th',
  `joining_date` DATE NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 5. Classes Table
-- --------------------------------------------------------
CREATE TABLE `classes` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `school_id` BIGINT UNSIGNED NOT NULL,
  `class_name` VARCHAR(50) NOT NULL, -- e.g. "9th"
  `numeric_order` INT NOT NULL DEFAULT 9,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uq_school_class` (`school_id`, `class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 6. Sections Table
-- --------------------------------------------------------
CREATE TABLE `sections` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `section_name` VARCHAR(20) NOT NULL DEFAULT 'A',
  `room_no` VARCHAR(50) NULL,
  `class_teacher_id` BIGINT UNSIGNED NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`class_id`) REFERENCES `classes`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`class_teacher_id`) REFERENCES `teachers`(`id`) ON DELETE SET NULL,
  UNIQUE KEY `uq_class_section` (`class_id`, `section_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 7. Students Table
-- --------------------------------------------------------
CREATE TABLE `students` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `local_uuid` VARCHAR(64) NOT NULL UNIQUE,
  `school_id` BIGINT UNSIGNED NOT NULL,
  `student_id` VARCHAR(50) NOT NULL UNIQUE,
  `admission_no` VARCHAR(50) NULL,
  `student_name` VARCHAR(150) NOT NULL,
  `father_name` VARCHAR(150) NULL,
  `mother_name` VARCHAR(150) NULL,
  `roll_number` VARCHAR(20) NOT NULL,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `section_id` BIGINT UNSIGNED NULL,
  `dob` DATE NULL,
  `gender` ENUM('Male', 'Female', 'Other') DEFAULT 'Male',
  `mobile` VARCHAR(20) NULL,
  `address` TEXT NULL,
  `photo_path` VARCHAR(255) NULL,
  `has_face_enrolled` TINYINT(1) DEFAULT 0,
  `status` ENUM('ACTIVE', 'INACTIVE', 'TRANSFERRED') DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_students_uuid` (`local_uuid`),
  KEY `idx_students_class` (`class_id`, `section_id`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`class_id`) REFERENCES `classes`(`id`) ON DELETE RESTRICT,
  FOREIGN KEY (`section_id`) REFERENCES `sections`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 8. Student Face Templates Table (MobileFaceNet 192D Vectors)
-- --------------------------------------------------------
CREATE TABLE `student_face_templates` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `student_uuid` VARCHAR(64) NOT NULL,
  `sample_angle` ENUM('FRONT', 'LEFT', 'RIGHT', 'UP', 'DOWN') DEFAULT 'FRONT',
  `embedding_json` LONGTEXT NOT NULL, -- Serialized JSON array of 192 float numbers
  `quality_score` DECIMAL(4,3) DEFAULT 1.000,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_face_student` (`student_id`),
  KEY `idx_face_uuid` (`student_uuid`),
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 9. Attendance Sessions Table
-- --------------------------------------------------------
CREATE TABLE `attendance_sessions` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `session_name` VARCHAR(50) NOT NULL, -- e.g. "MORNING", "AFTERNOON"
  `start_time` TIME NOT NULL DEFAULT '08:00:00',
  `end_time` TIME NOT NULL DEFAULT '11:00:00',
  `is_active` TINYINT(1) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 10. Devices Table (Authorized Attendance Tablets / Mobiles)
-- --------------------------------------------------------
CREATE TABLE `devices` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `school_id` BIGINT UNSIGNED NOT NULL,
  `device_identifier` VARCHAR(100) NOT NULL UNIQUE,
  `device_name` VARCHAR(100) NULL,
  `assigned_location` VARCHAR(100) NULL,
  `last_active` TIMESTAMP NULL,
  `is_authorized` TINYINT(1) DEFAULT 1,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 11. Attendance Table
-- --------------------------------------------------------
CREATE TABLE `attendance` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `local_uuid` VARCHAR(64) NOT NULL UNIQUE,
  `school_id` BIGINT UNSIGNED NOT NULL,
  `student_id` BIGINT UNSIGNED NOT NULL,
  `class_id` BIGINT UNSIGNED NOT NULL,
  `attendance_date` DATE NOT NULL,
  `attendance_time` TIME NOT NULL,
  `attendance_session` VARCHAR(30) DEFAULT 'MORNING',
  `attendance_status` ENUM('PRESENT', 'ABSENT', 'LATE', 'HALF_DAY', 'LEAVE') NOT NULL DEFAULT 'PRESENT',
  `attendance_method` ENUM('FACE', 'MANUAL', 'QR') NOT NULL DEFAULT 'FACE',
  `confidence` DECIMAL(4,3) DEFAULT 1.000,
  `liveness_score` DECIMAL(4,3) DEFAULT 1.000,
  `device_id` VARCHAR(100) DEFAULT 'ANDROID_DEVICE',
  `teacher_id` BIGINT UNSIGNED NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_att_date_class` (`attendance_date`, `class_id`),
  KEY `idx_att_student` (`student_id`),
  UNIQUE KEY `uq_student_daily_attendance` (`student_id`, `attendance_date`, `attendance_session`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`class_id`) REFERENCES `classes`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 12. Notifications Table
-- --------------------------------------------------------
CREATE TABLE `notifications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `school_id` BIGINT UNSIGNED NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `message` TEXT NOT NULL,
  `type` VARCHAR(50) DEFAULT 'INFO',
  `is_read` TINYINT(1) DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 13. Sync Logs Table
-- --------------------------------------------------------
CREATE TABLE `sync_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `device_id` VARCHAR(100) NOT NULL,
  `sync_type` ENUM('PUSH', 'PULL', 'FULL') DEFAULT 'PUSH',
  `records_processed` INT DEFAULT 0,
  `status` ENUM('SUCCESS', 'FAILED', 'PARTIAL') DEFAULT 'SUCCESS',
  `message` TEXT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 14. Audit Logs Table
-- --------------------------------------------------------
CREATE TABLE `audit_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NULL,
  `action` VARCHAR(100) NOT NULL,
  `entity_name` VARCHAR(50) NOT NULL,
  `entity_id` VARCHAR(64) NOT NULL,
  `ip_address` VARCHAR(45) NULL,
  `details` TEXT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- SEED INITIAL DATA
-- ========================================================

-- Insert Sample School
INSERT INTO `schools` (`id`, `school_code`, `school_name`, `address`, `city`, `state`, `contact_number`, `email`)
VALUES (1, 'SCH-2026', 'Central Model High School', 'Civil Lines, Main Road', 'Patna', 'Bihar', '+91 9876543210', 'admin@hajiribook.edu');

-- Insert Academic Session
INSERT INTO `academic_sessions` (`id`, `school_id`, `session_name`, `start_date`, `end_date`, `is_current`)
VALUES (1, 1, '2026-2027', '2026-04-01', '2027-03-31', 1);

-- Insert Users (Password: 123456 -> hashed with bcrypt)
-- $2y$10$wO3uP.s3g.gPq747pknNueuR7p0k54XbL30833l3z2BwX5E3d7w2O is bcrypt for 123456
INSERT INTO `users` (`id`, `school_id`, `username`, `password_hash`, `full_name`, `role`, `mobile`, `email`)
VALUES 
(1, 1, 'admin', '$2y$10$wO3uP.s3g.gPq747pknNueuR7p0k54XbL30833l3z2BwX5E3d7w2O', 'Principal Administrator', 'ADMIN', '9876543200', 'admin@school.com'),
(2, 1, 'madhurendra', '$2y$10$wO3uP.s3g.gPq747pknNueuR7p0k54XbL30833l3z2BwX5E3d7w2O', 'Madhurendra Kumar', 'TEACHER', '9876543210', 'madhurendra@school.com');

-- Insert Teacher Profile
INSERT INTO `teachers` (`id`, `user_id`, `employee_id`, `qualification`, `designation`, `assigned_classes`)
VALUES (1, 2, 'EMP-T-001', 'M.Sc., B.Ed.', 'Senior Teacher', '6th - 10th');

-- Insert Classes
INSERT INTO `classes` (`id`, `school_id`, `class_name`, `numeric_order`) VALUES
(1, 1, '6th', 6),
(2, 1, '7th', 7),
(3, 1, '8th', 8),
(4, 1, '9th', 9),
(5, 1, '10th', 10);

-- Insert Sections
INSERT INTO `sections` (`id`, `class_id`, `section_name`, `class_teacher_id`) VALUES
(1, 4, 'A', 1);

-- Insert Attendance Session
INSERT INTO `attendance_sessions` (`id`, `session_name`, `start_time`, `end_time`) VALUES
(1, 'MORNING', '08:00:00', '12:00:00');

-- Insert Sample Students (Matching Mockup)
INSERT INTO `students` (`id`, `local_uuid`, `school_id`, `student_id`, `admission_no`, `student_name`, `roll_number`, `class_id`, `section_id`, `dob`, `gender`, `mobile`, `has_face_enrolled`)
VALUES
(1, 'stu-uuid-001', 1, 'STU-2026-012', 'ADM-1012', 'Rahul Kumar', '12', 4, 1, '2010-05-14', 'Male', '9876543211', 1),
(2, 'stu-uuid-002', 1, 'STU-2026-013', 'ADM-1013', 'Aman Kumar', '13', 4, 1, '2010-06-20', 'Male', '9876543212', 1),
(3, 'stu-uuid-003', 1, 'STU-2026-014', 'ADM-1014', 'Priya Kumari', '14', 4, 1, '2010-09-08', 'Female', '9876543213', 1),
(4, 'stu-uuid-004', 1, 'STU-2026-015', 'ADM-1015', 'Suman Kumar', '15', 4, 1, '2010-03-25', 'Male', '9876543214', 1),
(5, 'stu-uuid-005', 1, 'STU-2026-016', 'ADM-1016', 'Rohit Kumar', '16', 4, 1, '2010-11-12', 'Male', '9876543215', 1);

-- Insert Today's Attendance (16 Nov 2026)
INSERT INTO `attendance` (`local_uuid`, `school_id`, `student_id`, `class_id`, `attendance_date`, `attendance_time`, `attendance_status`, `attendance_method`, `confidence`)
VALUES
('att-uuid-001', 1, 1, 4, '2026-11-16', '09:12:15', 'PRESENT', 'FACE', 0.940),
('att-uuid-002', 1, 2, 4, '2026-11-16', '09:14:02', 'PRESENT', 'FACE', 0.910),
('att-uuid-003', 1, 3, 4, '2026-11-16', '09:15:30', 'ABSENT', 'MANUAL', 1.000),
('att-uuid-004', 1, 4, 4, '2026-11-16', '09:16:45', 'PRESENT', 'FACE', 0.890),
('att-uuid-005', 1, 5, 4, '2026-11-16', '09:17:10', 'ABSENT', 'MANUAL', 1.000);
