# AI Gateway API

AI Gateway API is a Spring Boot backend service that provides a centralized gateway for interacting with Large Language Model (LLM) providers.

The project was developed as a backend-focused AI Gateway, providing authentication, conversation management, AI provider integration, structured AI responses, request tracking, usage statistics, timeout handling, retry mechanisms, logging, and per-user rate limiting.

**Production API:**

```text
https://aigateway-production-388a.up.railway.app
```

---

## Features

- JWT Authentication
- User registration and login
- MySQL database integration
- Conversation management
- Conversation ownership protection
- Google Gemini integration
- OpenAI provider support
- Structured AI output
- AI request tracking
- Input/output token tracking
- Request latency tracking
- Usage statistics
- Global exception handling
- Request validation
- Provider timeout handling
- Automatic retry mechanism
- Provider rate-limit handling
- Per-user Gateway rate limiting
- Application logging
- Production deployment on Railway

---

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server / JWT
- Spring Data JPA
- Hibernate
- MySQL 8
- Maven
- Google Gemini API
- OpenAI API support
- Postman
- Railway

---

# Architecture

The application follows a layered architecture.

Main request flow:

```text
Client / Postman
       │
       ▼
Spring Security
JWT Authentication
       │
       ▼
REST Controllers
       │
       ▼
Service Layer
       │
       ├──────────────► LLM Provider Layer
       │                     │
       │                     ├──► Google Gemini API
       │                     │
       │                     └──► OpenAI API
       │
       ▼
Repository Layer
       │
       ▼
Spring Data JPA / Hibernate
       │
       ▼
MySQL
```

The AI processing flow also includes:

```text
AI Request
    │
    ▼
Rate Limiting
    │
    ▼
LLM Provider
    │
    ├── Timeout Handling
    ├── Retry Mechanism
    └── Error Handling
    │
    ▼
Usage Tracking
    │
    ├── Tokens
    ├── Latency
    └── Status
```

## System Architecture Diagram

![System Architecture](docs/Architecture%20diagram.png)

---

# Database

The application uses MySQL for persistence.

For local development, the database is:

```text
ai_gateway_db
```

The production environment uses a Railway-hosted MySQL database.

The database contains four main tables:

- `users`
- `conversations`
- `messages`
- `ai_requests`

## Main Relationships

```text
User 1 ─────── N Conversation

Conversation 1 ─────── N Message

User 1 ─────── N AIRequest

Conversation 1 ─────── 0..N AIRequest
```

`ai_requests.conversation_id` is nullable because not every AI request belongs to a conversation.

For example:

```text
POST /api/v1/ai/chat
→ AI request is associated with a conversation.

POST /api/v1/ai/structured
→ AI request does not require a conversation.
```

## Database Schema

![Database Schema](docs/database-schema.png)

## AI Request Tracking

Each AI request records:

- User
- Request ID
- Model
- Timestamp
- Latency
- Input tokens
- Output tokens
- Status
- Error code when applicable
- Conversation when applicable

Supported AI request statuses:

```text
SUCCESS
ERROR
TIMEOUT
RATE_LIMITED
```

---

# Project Structure

```text
ai-gateway/
│
├── docs/
│   ├── Architecture diagram.png
│   ├── database-schema.png
│   └── API_EXAMPLES.md
│
├── src/
│   ├── main/
│   │   ├── java/com/baotrung/ai_gateway/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── provider/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
├── README.md
└── AI_WORKLOG.md
```

---

# Configuration

## Environment Variables

Sensitive configuration such as API keys, JWT secrets, and database credentials must not be committed directly to the repository.

The application supports the following environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
GEMINI_API_KEY
OPENAI_API_KEY
PORT
```

`OPENAI_API_KEY` is optional when Google Gemini is used as the primary provider.

Example Spring configuration:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/ai_gateway_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

server.port=${PORT:8080}

jwt.secret=${JWT_SECRET}

gemini.api-key=${GEMINI_API_KEY:}
openai.api-key=${OPENAI_API_KEY:}
```

> Never commit real API keys, JWT secrets, or database passwords to Git.

---

## Gemini Configuration

```properties
gemini.base-url=https://generativelanguage.googleapis.com
gemini.api-key=${GEMINI_API_KEY:}
gemini.model=gemini-3.5-flash-lite

gemini.connect-timeout-ms=5000
gemini.read-timeout-ms=30000
gemini.max-retries=3
```

Google Gemini is currently used as the primary working AI provider.

---

## Gateway Rate Limiting

Default configuration:

```properties
rate-limit.max-requests=10
rate-limit.window-seconds=60
```

The current implementation uses an in-memory per-user sliding-window rate limiter.

Each authenticated user can make up to:

```text
10 AI requests / 60 seconds
```

For a distributed production environment, a shared rate-limiting solution such as Redis could be used instead.

---

# Running Locally

## Requirements

Make sure the following software is installed:

- Java 21
- MySQL 8
- Maven or Maven Wrapper
- Postman
- A valid Gemini API key

---

## 1. Clone the Repository

```bash
git clone https://github.com/Trung-Nguyen909/AI_gateway.git
cd AI_gateway/ai-gateway
```

---

## 2. Create MySQL Database

```sql
CREATE DATABASE ai_gateway_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

---

## 3. Configure Local Environment Variables

The application uses environment variables for sensitive configuration.

Configure:

```text
DB_PASSWORD=<your-local-mysql-password>
JWT_SECRET=<your-local-jwt-secret>
GEMINI_API_KEY=<your-gemini-api-key>
```

Optional:

```text
OPENAI_API_KEY=<your-openai-api-key>
```

For IntelliJ IDEA, these variables can be configured in:

```text
Run
→ Edit Configurations
→ AiGatewayApplication
→ Environment variables
```

The local database configuration defaults to:

```text
Host:     localhost
Port:     3306
Database: ai_gateway_db
Username: root
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate updates the required tables based on the application's JPA entities.

---

## 4. Run the Application

Using Maven Wrapper on Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

The application can also be run directly from IntelliJ IDEA.

The local API is available at:

```text
http://localhost:8080
```

---

# API Documentation

Most API endpoints are protected by JWT authentication.

Protected requests must include:

```http
Authorization: Bearer <access_token>
```

---

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

---

### Login

```http
POST /api/v1/auth/login
```

Authenticates the user and returns a JWT access token.

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

The returned token must be included in subsequent protected API requests.

---

## Conversations

Conversation endpoints require authentication.

### Create Conversation

```http
POST /api/v1/conversations
```

Example request:

```json
{
  "title": "Spring Boot Learning"
}
```

Creates a conversation for the authenticated user.

---

### Get Conversations

```http
GET /api/v1/conversations
```

Returns conversations belonging to the authenticated user.

---

### Get Conversation by ID

```http
GET /api/v1/conversations/{id}
```

Returns a specific conversation.

Conversation ownership is enforced. A user cannot access another user's conversation.

---

# AI Chat

## Send AI Chat Request

```http
POST /api/v1/ai/chat
```

Requires JWT authentication.

Example request:

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

The Gateway:

1. Authenticates the user.
2. Verifies conversation ownership.
3. Loads previous conversation messages.
4. Sends the request to the selected LLM provider.
5. Stores the user and assistant messages.
6. Records usage information in `ai_requests`.
7. Returns the AI response.

---

# Structured AI Output

## Generate Structured Response

```http
POST /api/v1/ai/structured
```

Requires JWT authentication.

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

The LLM is instructed to return JSON in a predefined structure.

The response is parsed into a Java DTO before being returned to the client.

Structured AI requests are also recorded in `ai_requests`.

Because this endpoint does not require a conversation:

```text
conversation_id = NULL
```

is valid for these request records.

---

# Usage Statistics

## Get Usage Statistics

```http
GET /api/v1/usage
```

Requires JWT authentication.

Returns usage statistics belonging to the authenticated user.

Example response:

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

Statistics include:

- Total AI requests
- Total input tokens
- Total output tokens
- Total tokens
- Average latency
- Error rate

Usage data is calculated from AI request records belonging to the authenticated user.

---

# Reliability

## Timeout Handling

Gemini requests have separate connection and read timeouts.

Default configuration:

```text
Connection timeout: 5 seconds
Read timeout:       30 seconds
```

A provider timeout is recorded as:

```text
TIMEOUT
```

---

## Retry Mechanism

Temporary provider failures can be retried automatically.

Current maximum:

```text
3 attempts
```

Retry is used for temporary conditions such as:

- Provider HTTP 429 responses
- Provider 5xx errors
- Network/resource access failures

---

## Provider Rate Limiting

If the external AI provider returns HTTP `429`, the Gateway retries the request according to the configured retry policy.

If the request still fails because of provider rate limiting, the request is recorded as:

```text
RATE_LIMITED
```

---

## Gateway Rate Limiting

The application also provides per-user rate limiting before sending requests to the AI provider.

Default:

```text
10 requests / 60 seconds / user
```

When the limit is exceeded, the API returns:

```http
HTTP 429 Too Many Requests
```

The current rate limiter is stored in application memory and is intended for a single application instance.

---

# Error Handling

The application uses centralized exception handling to provide consistent API error responses.

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

Common HTTP status codes:

| Status | Description |
|---|---|
| `400` | Invalid request or validation error |
| `401` | Authentication required or invalid token |
| `404` | Resource not found or inaccessible |
| `429` | Gateway rate limit exceeded |
| `500` | Unexpected server error |

---

# AI Request Tracking

AI requests are stored in the `ai_requests` table.

Tracked information includes:

```text
user
request ID
model
timestamp
latency
input tokens
output tokens
status
error code
conversation (optional)
```

Possible statuses:

| Status | Description |
|---|---|
| `SUCCESS` | AI request completed successfully |
| `ERROR` | AI request failed because of another error |
| `TIMEOUT` | AI provider request timed out |
| `RATE_LIMITED` | AI provider rate limit was reached |

This information is used to calculate user usage statistics.

---

# Logging

The application uses SLF4J-based application logging.

Logs include information such as:

- Provider calls
- Model being used
- Retry attempts
- Retry delays
- Application errors

Sensitive information such as API keys is not intentionally written to application logs.

---

# Security

The project implements the following security measures:

- Passwords are hashed using BCrypt.
- JWT authentication is used for protected endpoints.
- Authentication is stateless.
- Conversation access is scoped to the authenticated user.
- AI usage statistics are scoped to the authenticated user.
- API keys and credentials are loaded through environment variables.
- Sensitive credentials are not committed to the repository.
- API keys are not intentionally included in application logs.
- Request validation is performed before processing invalid input.

---

# Multi-Provider Design

The application defines a common LLM provider abstraction.

```text
LLMProvider
    │
    ├── GeminiProvider
    │
    └── OpenAIProvider
```

This design allows additional AI providers to be integrated without changing the main AI Gateway API contract.

Google Gemini is currently used as the primary working provider.

OpenAI provider integration is also implemented, but successful requests require an OpenAI account/API key with available API credits.

---

# Development Notes

Hibernate currently uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This configuration is convenient for development and demonstration because Hibernate can update the schema based on JPA entities without recreating the entire database on every application start.

For a larger production system, schema changes should preferably be managed through database migration tools such as:

- Flyway
- Liquibase

A production environment could then use:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

after database migrations are managed separately.

The current Gateway rate limiter is also in memory. A distributed deployment with multiple application instances would require a shared rate-limiting store such as Redis.

---

# Deployment

The AI Gateway is deployed on Railway.

## Production API

```text
https://aigateway-production-388a.up.railway.app
```

The production environment uses:

- **Railway** for Spring Boot application deployment
- **Railway MySQL** for database persistence
- **Google Gemini** as the primary AI provider
- **GitHub** as the deployment source
- **Environment variables** for sensitive configuration

The GitHub repository contains the Spring Boot application inside the `ai-gateway` directory, which is configured as the application root directory for the Railway service.

The production database is independent from the local development MySQL database.

The Spring Boot service connects to Railway MySQL through environment variables and Railway service references.

Production configuration includes:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
GEMINI_API_KEY
```

`OPENAI_API_KEY` can optionally be configured when OpenAI API access is available.

Sensitive values such as database credentials, JWT secrets, and API keys are stored as Railway environment variables and are not committed to GitHub.

## Production Endpoints

Authentication:

```text
POST https://aigateway-production-388a.up.railway.app/api/v1/auth/register
POST https://aigateway-production-388a.up.railway.app/api/v1/auth/login
```

Conversations:

```text
POST https://aigateway-production-388a.up.railway.app/api/v1/conversations
GET  https://aigateway-production-388a.up.railway.app/api/v1/conversations
GET  https://aigateway-production-388a.up.railway.app/api/v1/conversations/{id}
```

AI:

```text
POST https://aigateway-production-388a.up.railway.app/api/v1/ai/chat
POST https://aigateway-production-388a.up.railway.app/api/v1/ai/structured
```

Usage:

```text
GET https://aigateway-production-388a.up.railway.app/api/v1/usage
```

The production API was verified using Postman after deployment.

---

# API Testing

The API can be tested using Postman or another REST API client.

## Base URLs

Local development:

```text
http://localhost:8080
```

Production:

```text
https://aigateway-production-388a.up.railway.app
```

In Postman, the `base_url` collection variable can be switched between the local and production URLs.

Protected endpoints require a JWT access token:

```http
Authorization: Bearer <access_token>
```

## Recommended Test Flow

```text
Register
   ↓
Login
   ↓
Receive JWT Access Token
   ↓
Create Conversation
   ↓
Get Conversations
   ↓
Get Conversation By ID
   ↓
AI Chat
   ↓
Structured AI
   ↓
Get Usage Statistics
```

Suggested Postman collection variables:

```text
base_url        = Local or production API base URL
access_token    = JWT returned from Login
conversation_id = ID returned from Create Conversation
```

Example request URL:

```text
{{base_url}}/api/v1/ai/chat
```

Bearer token:

```text
{{access_token}}
```

Example AI Chat body:

```json
{
  "conversationId": 1,
  "message": "Explain Dependency Injection in Spring Boot.",
  "model": "gemini-3.5-flash-lite"
}
```

When using the Postman collection, the example conversation ID can be replaced with the `{{conversation_id}}` collection variable.

Detailed request and response examples for Authentication, Conversations, AI Chat, Structured AI, Usage Statistics, validation, rate limiting, and timeout handling are available here:

[View API Request & Response Examples](docs/API_EXAMPLES.md)

> JWT tokens, API keys, JWT secrets, and database passwords must never be committed to the repository.

---

# Project Deliverables

The project deliverables include:

- **Backend Source Code** — Complete Spring Boot source code for the AI Gateway.
- **API Documentation** — API endpoints, authentication, request formats, deployment, and usage instructions are documented in this README.
- **API Request & Response Examples** — Detailed testing examples are available in [API Examples](docs/API_EXAMPLES.md).
- **Architecture Diagram** — System architecture is documented in [Architecture Diagram](docs/Architecture%20diagram.png).
- **Database Schema** — Database structure and relationships are documented in [Database Schema](docs/database-schema.png).
- **AI Development Worklog** — The AI-assisted development process, implementation decisions, debugging, and verification are documented in [AI Development Worklog](AI_WORKLOG.md).
- **Deployment** — The AI Gateway is deployed on Railway and accessible through the production API URL provided in the Deployment section.
- **Demo Video** — A short demonstration video will be provided separately with the final submission.

---

# Future Improvements

Possible improvements for a production-ready version include:

- Redis-based distributed rate limiting
- Automatic provider fallback
- Dynamic model routing
- Response caching
- AI cost estimation
- Metrics and observability
- Flyway or Liquibase database migrations
- Queue-based asynchronous AI processing
- Distributed request tracing
- More comprehensive automated testing

---

# Author

**Nguyen Bao Trung**

Backend Developer Candidate

---

## License

This project was developed as part of an AI Builder / Backend Developer technical challenge.