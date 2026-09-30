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
USE ai_gateway_db;

SELECT
    id,
    user_id,
    title,
    created_at,
    updated_at
FROM conversations;
SELECT id, user_id, title
FROM conversations
ORDER BY id;
USE ai_gateway_db;
SELECT
    id,
    request_id,
    user_id,
    conversation_id,
    model,
    timestamp,
    latency_ms,
    input_tokens,
    output_tokens,
    status,
    error_code
FROM ai_requests
ORDER BY id DESC;

SELECT
    id,
    conversation_id,
    role,
    content,
    created_at
FROM messages
ORDER BY id DESC;
SELECT
    id,
    user_id,
    request_id,
    model,
    latency_ms,
    input_tokens,
    output_tokens,
    status
FROM ai_requests
ORDER BY id;
SELECT
    id,
    request_id,
    model,
    latency_ms,
    status,
    error_code
FROM ai_requests
ORDER BY id DESC;
SELECT
    id,
    user_id,
    conversation_id,
    request_id,
    model,
    timestamp,
    latency_ms,
    input_tokens,
    output_tokens,
    status,
    error_code
FROM ai_requests
ORDER BY id DESC;
SELECT * FROM users;
SELECT * FROM conversations;
SELECT * FROM messages;

SELECT
    id,
    user_id,
    conversation_id,
    request_id,
    model,
    timestamp,
    latency_ms,
    input_tokens,
    output_tokens,
    status,
    error_code
FROM ai_requests
ORDER BY id DESC;