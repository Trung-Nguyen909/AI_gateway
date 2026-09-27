CREATE DATABASE ai_gateway_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
USE ai_gateway_db;
SELECT DATABASE();
SHOW TABLES;
DESCRIBE conversations;
DESCRIBE messages;
DESCRIBE ai_requests;
SHOW CREATE TABLE conversations;
DESCRIBE users;
SELECT VERSION();
SELECT id, email, password_hash, role, created_at
FROM users;