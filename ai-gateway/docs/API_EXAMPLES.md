# AI Gateway API Examples

This document provides example requests for testing the AI Gateway API.

The examples can be executed using Postman or another REST API client.

## Base URL

Local development:

```text
http://localhost:8080
```

All protected endpoints require a JWT access token:

```http
Authorization: Bearer <access_token>
```

---

# 1. Authentication

## 1.1 Register

### Request

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "email": "demo@example.com",
  "password": "password123"
}
```

This endpoint does not require authentication.

---

## 1.2 Login

### Request

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "demo@example.com",
  "password": "password123"
}
```

### Example Response

```json
{
  "accessToken": "<JWT_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Use the returned `accessToken` for protected endpoints.

Example:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# 2. Conversations

All conversation endpoints require JWT authentication.

## 2.1 Create Conversation

### Request

```http
POST /api/v1/conversations
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "title": "Spring Boot Learning"
}
```

### Example Response

```json
{
  "id": 1,
  "title": "Spring Boot Learning",
  "createdAt": "2026-10-01T00:00:00",
  "updatedAt": "2026-10-01T00:00:00"
}
```

Save the returned `id` because it will be used as the `conversationId` in AI chat requests.

---

## 2.2 Get Conversations

### Request

```http
GET /api/v1/conversations
Authorization: Bearer <JWT_TOKEN>
```

Returns conversations belonging to the authenticated user.

### Example Response

```json
[
  {
    "id": 1,
    "title": "Spring Boot Learning",
    "createdAt": "2026-10-01T00:00:00",
    "updatedAt": "2026-10-01T00:00:00"
  }
]
```

---

## 2.3 Get Conversation By ID

### Request

```http
GET /api/v1/conversations/1
Authorization: Bearer <JWT_TOKEN>
```

The authenticated user can only access conversations belonging to their own account.

---

# 3. AI Chat

## 3.1 Send AI Chat Request

### Request

```http
POST /api/v1/ai/chat
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "conversationId": 1,
  "message": "Explain Dependency Injection in Spring Boot.",
  "model": "gemini-3.5-flash-lite"
}
```

### Example Response

```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "conversationId": 1,
  "model": "gemini-3.5-flash-lite",
  "content": "Dependency Injection is a design pattern...",
  "inputTokens": 25,
  "outputTokens": 100,
  "latencyMs": 1250
}
```

The values above are examples. Token counts and latency depend on the actual provider response.

For every processed AI request, the Gateway records information such as:

```text
user
request ID
model
timestamp
latency
input tokens
output tokens
status
conversation
```

The user and assistant messages are also stored as conversation history.

---

# 4. Structured AI Output

## 4.1 Generate Structured Response

### Request

```http
POST /api/v1/ai/structured
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "topic": "Dependency Injection in Spring Boot"
}
```

### Example Response

```json
{
  "title": "Dependency Injection in Spring Boot",
  "summary": "Dependency Injection is a core concept used by Spring to manage dependencies between application components.",
  "keyPoints": [
    "It reduces coupling between components",
    "Dependencies are managed by the Spring container",
    "Constructor injection is commonly used"
  ]
}
```

The AI provider is instructed to return JSON using a predefined structure.

The response is parsed into a Java DTO before being returned by the API.

Structured AI requests are also recorded in the `ai_requests` table.

Unlike AI Chat, a structured request does not require a conversation, so:

```text
conversation_id = NULL
```

is valid.

---

# 5. Usage Statistics

## 5.1 Get Usage

### Request

```http
GET /api/v1/usage
Authorization: Bearer <JWT_TOKEN>
```

### Example Response

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

The values are calculated from AI requests belonging to the authenticated user.

The statistics include:

- Total requests
- Total input tokens
- Total output tokens
- Total tokens
- Average latency
- Error rate

---

# 6. Validation Error Example

Sending an invalid request can return HTTP `400`.

Example invalid structured AI request:

```http
POST /api/v1/ai/structured
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "topic": ""
}
```

Example error structure:

```json
{
  "timestamp": "2026-10-01T00:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/ai/structured"
}
```

The exact validation message can depend on the rejected field.

---

# 7. Conversation Ownership Protection

A user cannot access a conversation belonging to another user.

Example:

```http
GET /api/v1/conversations/1
Authorization: Bearer <ANOTHER_USER_JWT>
```

If conversation `1` belongs to a different user, the API returns an error such as:

```http
HTTP 404 Not Found
```

This prevents users from accessing other users' conversation data.

---

# 8. Gateway Rate Limit Example

The default Gateway rate limit is:

```text
10 AI requests / 60 seconds / user
```

When the authenticated user exceeds the configured limit, the Gateway returns:

```http
HTTP 429 Too Many Requests
```

Example error response:

```json
{
  "timestamp": "2026-10-01T00:00:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many AI requests. Please try again later.",
  "path": "/api/v1/ai/chat"
}
```

The current rate limiter is implemented in memory and is intended for the single-instance demo application.

---

# 9. Provider Timeout

Gemini provider requests use configured connection and read timeouts.

Current configuration:

```properties
gemini.connect-timeout-ms=5000
gemini.read-timeout-ms=30000
gemini.max-retries=3
```

If the provider request times out after the configured retry attempts, the AI request is recorded with:

```text
status = TIMEOUT
```

---

# 10. Provider Rate Limit

When the external AI provider returns HTTP `429`, the Gateway retries the request according to the configured retry policy.

If all retry attempts fail because of provider rate limiting, the request is recorded with:

```text
status = RATE_LIMITED
```

---

# 11. Suggested Postman Test Flow

The recommended API testing order is:

```text
Register
   ↓
Login
   ↓
Receive JWT
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
Get Usage
```

Suggested Postman variables:

```text
base_url       = http://localhost:8080
access_token   = <JWT returned from Login>
conversation_id = <ID returned from Create Conversation>
```

Example request URL using a Postman variable:

```text
{{base_url}}/api/v1/ai/chat
```

Example Bearer Token:

```text
{{access_token}}
```

Example AI Chat body:

```json
{
  "conversationId": {{conversation_id}},
  "message": "Explain Dependency Injection in Spring Boot.",
  "model": "gemini-3.5-flash-lite"
}
```

---

# Notes

- The numerical values shown in responses are examples and can differ between executions.
- JWT tokens must not be committed to the repository.
- Gemini and OpenAI API keys must not be committed to the repository.
- Database passwords must not be committed to the repository.
- Google Gemini is currently the primary working AI provider.
- OpenAI provider support is implemented but requires an API key/account with available API credits.