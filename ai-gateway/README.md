# AI Gateway API

AI Gateway API is a Spring Boot backend service that provides a centralized gateway for interacting with Large Language Model (LLM) providers.

The system provides authentication, conversation management, AI provider integration, structured AI responses, request tracking, usage statistics, timeout handling, retry mechanisms, logging, and per-user rate limiting.

## Features

- JWT Authentication
- User registration and login
- MySQL database integration
- Conversation management
- Conversation ownership protection
- Gemini LLM integration
- OpenAI provider support
- Structured AI output
- AI request tracking
- Input/output token tracking
- Request latency tracking
- Usage statistics
- Global error handling
- Request validation
- Provider timeout handling
- Automatic retry
- Provider rate-limit handling
- Per-user Gateway rate limiting
- Application logging

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server / JWT
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Gemini API
- OpenAI API support
- Postman

## Architecture

The application follows a layered architecture:

Client

↓

REST Controller

↓

Service Layer

↓

AI Gateway / LLM Provider

↓

Gemini API / OpenAI API

The service layer also communicates with the persistence layer:

Service

↓

Repository

↓

Spring Data JPA / Hibernate

↓

MySQL

Detailed architecture diagram is included separately in the project documentation.

## Database

Database name:

`ai_gateway_db`

Main tables:

- `users`
- `conversations`
- `messages`
- `ai_requests`

Main relationships:

- User 1:N Conversation
- Conversation 1:N Message
- User 1:N AIRequest
- AIRequest optionally belongs to a Conversation

Each AI request records:

- User
- Model
- Timestamp
- Latency
- Input tokens
- Output tokens
- Status
- Error code when applicable

AI request statuses:

- `SUCCESS`
- `ERROR`
- `TIMEOUT`
- `RATE_LIMITED`

## Configuration

The application uses environment variables for secrets.

Example configuration:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

openai.api-key=${OPENAI_API_KEY:}
gemini.api-key=${GEMINI_API_KEY:}
```

Do not commit real API keys or database passwords to Git.

### Gemini Reliability Configuration

```properties
gemini.connect-timeout-ms=5000
gemini.read-timeout-ms=30000
gemini.max-retries=3
```

### Gateway Rate Limiting

```properties
rate-limit.max-requests=10
rate-limit.window-seconds=60
```

The current rate limiter is an in-memory per-user sliding-window implementation intended for this demo/single-instance application.

For a distributed production environment, a shared solution such as Redis should be used.

## Running Locally

### Requirements

- Java 21
- Maven
- MySQL 8
- Gemini API key

### 1. Create Database

```sql
CREATE DATABASE ai_gateway_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 2. Configure Database

Configure the datasource in `application.properties` or through environment variables.

Local example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_gateway_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Configure Gemini API Key

Set the environment variable:

```text
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

Never commit the real key to the repository.

### 4. Run Application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API runs locally at:

```text
http://localhost:8080
```

## Authentication

Protected endpoints require a JWT access token.

Send the token using:

```http
Authorization: Bearer <access_token>
```

JWTs are generated after successful login.

---

# API Documentation

## Authentication

### Register

```http
POST /api/v1/auth/register
```

Creates a new user account.

Example request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Login

```http
POST /api/v1/auth/login
```

Authenticates a user and returns a JWT access token.

Example request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Example response:

```json
{
  "accessToken": "<JWT_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

## Conversations

All conversation endpoints require authentication.

### Create Conversation

```http
POST /api/v1/conversations
```

### Get Conversations

```http
GET /api/v1/conversations
```

Returns conversations belonging to the authenticated user.

### Get Conversation

```http
GET /api/v1/conversations/{id}
```

Users can only access conversations that belong to their own account.

---

## AI Chat

```http
POST /api/v1/ai/chat
```

Requires authentication.

Example:

```json
{
  "conversationId": 1,
  "message": "Explain Dependency Injection in Spring Boot.",
  "model": "gemini-3.5-flash-lite"
}
```

Example response:

```json
{
  "requestId": "generated-request-id",
  "conversationId": 1,
  "model": "gemini-3.5-flash-lite",
  "content": "Dependency Injection is...",
  "inputTokens": 25,
  "outputTokens": 120,
  "latencyMs": 1350
}
```

The Gateway stores both user and assistant messages and records AI usage information.

---

## Structured AI Output

```http
POST /api/v1/ai/structured
```

Requires authentication.

Example request:

```json
{
  "topic": "Dependency Injection in Spring Boot"
}
```

Example response:

```json
{
  "title": "Dependency Injection in Spring Boot",
  "summary": "Dependency Injection is a core Spring concept...",
  "keyPoints": [
    "Reduces coupling between components",
    "Dependencies are provided by the Spring container",
    "Constructor injection is commonly recommended"
  ]
}
```

The AI response is parsed into a predefined Java response structure before being returned to the client.

---

## Usage Statistics

```http
GET /api/v1/usage
```

Requires authentication.

Returns usage statistics for the authenticated user only.

Example:

```json
{
  "totalRequests": 5,
  "totalInputTokens": 120,
  "totalOutputTokens": 350,
  "totalTokens": 470,
  "averageLatencyMs": 1250.4,
  "errorRate": 20.0
}
```

---

## Error Responses

Errors use a consistent JSON structure.

Example:

```json
{
  "timestamp": "2026-09-30T22:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Conversation not found",
  "path": "/api/v1/ai/chat"
}
```

Common HTTP statuses:

| Status | Meaning |
|---|---|
| 400 | Invalid request / validation error |
| 401 | Authentication required or invalid token |
| 404 | Resource not found or inaccessible |
| 429 | Gateway rate limit exceeded |
| 500 | Unexpected server error |

## Reliability

### Timeout

Requests to the Gemini provider have configured connection and read timeouts.

### Retry

Temporary provider failures are retried up to a configured maximum number of attempts.

The current configuration uses:

```text
3 attempts maximum
```

### Provider Rate Limit

Provider HTTP `429` responses are detected and recorded using:

```text
RATE_LIMITED
```

### Gateway Rate Limit

The Gateway limits the number of AI requests each authenticated user can make within a configured time window.

Default:

```text
10 requests / 60 seconds / user
```

When exceeded:

```http
HTTP 429 Too Many Requests
```

## Usage Tracking

Every AI request handled by the AI endpoints records:

```text
user
model
timestamp
latency
input tokens
output tokens
status
```

Successful requests use:

```text
SUCCESS
```

Provider timeout:

```text
TIMEOUT
```

Provider rate limit:

```text
RATE_LIMITED
```

Other provider/application failures:

```text
ERROR
```

## Security

- Passwords are stored using BCrypt hashing.
- Protected endpoints require JWT authentication.
- Conversation queries are scoped to the authenticated user.
- API keys are loaded through environment variables.
- API keys are never included in application logs.
- The application uses stateless authentication.

## Development Notes

Hibernate is currently configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This is convenient for development and demonstration.

For a production system, database schema changes should preferably be managed using a migration tool such as Flyway or Liquibase.

## Deployment

Production API:

```text
TO_BE_ADDED_AFTER_DEPLOYMENT
```

Deployment configuration uses environment variables for database credentials and API keys.

## Project Deliverables

This repository contains or will include:

- Source code
- API documentation
- Architecture diagram
- Database schema
- Postman API examples
- Deployment instructions
- AI development worklog
- Demo video

## Future Improvements

Possible production improvements include:

- Redis-based distributed rate limiting
- Provider fallback and automatic model routing
- Response caching
- Cost estimation
- Metrics and observability
- Database migrations with Flyway/Liquibase
- Queue-based asynchronous AI processing