-- Classroom Presence System — database schema
-- Import with: mysql -u root < schema.sql   (or via phpMyAdmin)

CREATE DATABASE IF NOT EXISTS cps
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cps;

-- Users: teachers, students and dashboard admins share one table, separated by `role`.
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('teacher', 'student', 'admin') NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    student_id    VARCHAR(40)  NULL,
    course        VARCHAR(120) NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_user_role (username, role)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS courses (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    teacher_id INT NULL,
    UNIQUE KEY uniq_course (name),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Attendance records uploaded from the teacher app.
-- `attend_date` is a generated column used by the UNIQUE key to dedupe one tap
-- per student, per teacher, per day.
CREATE TABLE IF NOT EXISTS attendance (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    student_id   VARCHAR(40)  NOT NULL,
    student_name VARCHAR(120) NOT NULL,
    course       VARCHAR(120) NULL,
    class_name   VARCHAR(120) NULL,
    teacher_id   VARCHAR(40)  NOT NULL,
    timestamp    DATETIME     NOT NULL,
    attend_date  DATE AS (DATE(timestamp)) STORED,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_attendance (student_id, teacher_id, attend_date),
    INDEX idx_course (course),
    INDEX idx_timestamp (timestamp),
    INDEX idx_teacher (teacher_id)
) ENGINE=InnoDB;
