# AI Development Worklog

## Project

**AI Gateway API**

This document records how AI-assisted development was used during the implementation of the AI Gateway API.

AI was used as a development assistant for architecture discussion, debugging, implementation guidance, documentation, and testing suggestions.

All generated suggestions were reviewed, adapted to the actual project structure, and tested before being included in the project.

---

# Day 1 — Project Setup and Database

## Goals

- Understand the AI Gateway requirements
- Create the Spring Boot project
- Configure Java and Maven
- Connect the application to MySQL
- Design the initial database model

## Work Completed

The Spring Boot project was configured using:

- Java 21
- Spring Boot
- Maven
- Spring Data JPA
- Spring Security
- MySQL

The MySQL database was created:

```text
ai_gateway_db
```

The initial data model included:

```text
User
Conversation
Message
AIRequest
```

The main relationships were designed as:

```text
User 1:N Conversation

Conversation 1:N Message

User 1:N AIRequest
```

## AI Assistance

AI was used to:

- Review the project requirements
- Suggest a layered backend architecture
- Discuss the database entities and relationships
- Help configure Spring Boot with MySQL
- Explain Hibernate schema configuration

## Verification

The database connection was tested by starting the Spring Boot application and checking that Hibernate could connect to MySQL and create/update the required tables.

---

# Day 2 — Authentication and Security

## Goals

- Implement user registration
- Hash passwords securely
- Implement login
- Generate JWT access tokens
- Protect API endpoints

## Work Completed

Authentication was implemented using:

```text
Spring Security
BCrypt
JWT
OAuth2 Resource Server
```

The following authentication endpoints were implemented:

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
```

JWT tokens contain information used to identify the authenticated user.

The application uses stateless authentication.

Protected endpoints require:

```http
Authorization: Bearer <JWT_TOKEN>
```

## AI Assistance

AI was used to:

- Explain Spring Security configuration
- Help configure JWT encoding and decoding
- Suggest BCrypt for password hashing
- Diagnose authentication issues
- Explain stateless authentication

## Verification

Authentication was tested using Postman.

Tests included:

- Registering a new user
- Logging in
- Receiving a JWT
- Calling protected endpoints
- Testing requests without authentication

---

# Day 3 — Conversation Management

## Goals

- Create conversations
- Retrieve user conversations
- Store messages
- Protect conversation ownership

## Work Completed

The following endpoints were implemented:

```http
POST /api/v1/conversations
GET /api/v1/conversations
GET /api/v1/conversations/{id}
```

Conversation queries are scoped to the authenticated user.

A user cannot access a conversation belonging to another user.

The database relationships are:

```text
User
  │
  └── Conversation
          │
          └── Message
```

## AI Assistance

AI was used to:

- Review the conversation service design
- Suggest repository queries for ownership checking
- Explain how to obtain the authenticated user from JWT
- Help diagnose authentication parameter issues

One issue occurred when attempting to obtain authentication using an unsuitable controller parameter configuration.

The implementation was corrected to use `JwtAuthenticationToken` directly in the controller.

## Verification

Two different users were created to test ownership.

A user attempting to access another user's conversation was rejected.

---

# Day 4 — LLM Provider Integration

## Goals

- Create a provider abstraction
- Integrate an external LLM
- Support multiple AI providers
- Connect AI responses with conversations

## Work Completed

A common provider interface was introduced:

```text
LLMProvider
```

Provider implementations include:

```text
GeminiProvider
OpenAIProvider
```

Google Gemini became the primary working provider.

The configured Gemini model is:

```text
gemini-3.5-flash-lite
```

The AI chat endpoint was implemented:

```http
POST /api/v1/ai/chat
```

The AI chat flow is:

```text
Authenticated User
        ↓
Conversation Ownership Check
        ↓
Load Conversation History
        ↓
Build LLM Request
        ↓
LLM Provider
        ↓
Gemini API
        ↓
Store Messages
        ↓
Store AI Request Metrics
        ↓
Return Response
```

## AI Assistance

AI was used to:

- Discuss provider abstraction design
- Help implement the Gemini REST request
- Review Gemini request/response mapping
- Diagnose provider configuration problems
- Help investigate OpenAI API errors

OpenAI integration returned an API credit-related error during testing, so Gemini was used as the primary working provider.

## Verification

Gemini was tested both directly and through the AI Gateway.

Successful responses were returned through:

```http
POST /api/v1/ai/chat
```

---

# Day 5 — Usage Tracking and Error Handling

## Goals

- Track every AI request
- Store token usage
- Store latency
- Store request status
- Create usage statistics
- Standardize API errors

## Work Completed

AI requests are recorded in:

```text
ai_requests
```

Tracked fields include:

```text
user
request_id
model
timestamp
latency_ms
input_tokens
output_tokens
status
error_code
conversation_id
```

Supported statuses include:

```text
SUCCESS
ERROR
TIMEOUT
RATE_LIMITED
```

A usage endpoint was implemented:

```http
GET /api/v1/usage
```

It returns:

```text
totalRequests
totalInputTokens
totalOutputTokens
totalTokens
averageLatencyMs
errorRate
```

A global exception handler was also introduced to return consistent error responses.

## AI Assistance

AI was used to:

- Review the AI request tracking design
- Discuss how usage statistics should be calculated
- Help design the usage response DTO
- Suggest centralized exception handling
- Diagnose HTTP status and validation issues

## Verification

Usage isolation was tested with different users.

Each user only receives usage statistics calculated from their own AI requests.

Validation and resource-access errors were also tested.

---

# Day 6 — Reliability and Rate Limiting

## Goals

- Add provider timeout handling
- Add automatic retry
- Handle provider rate limits
- Add logging
- Add Gateway rate limiting

## Work Completed

Gemini reliability configuration includes:

```properties
gemini.connect-timeout-ms=5000
gemini.read-timeout-ms=30000
gemini.max-retries=3
```

Retry handling was added for temporary failures such as:

- HTTP 429
- HTTP 5xx
- Resource/network access failures

Custom provider exceptions were introduced for:

```text
ProviderTimeoutException
ProviderRateLimitException
```

Logging was implemented using SLF4J.

The application does not intentionally log API keys.

A per-user Gateway rate limiter was also implemented.

Default configuration:

```properties
rate-limit.max-requests=10
rate-limit.window-seconds=60
```

The current implementation uses an in-memory sliding window.

## AI Assistance

AI was used to:

- Discuss timeout configuration
- Design the retry mechanism
- Diagnose a missing configuration property during testing
- Suggest provider exception classification
- Replace temporary console output with structured logging
- Discuss a simple rate-limiting design suitable for the challenge

## Verification

Timeout handling was tested by temporarily reducing the Gemini read timeout.

The provider retried the request and the failed AI request was recorded with:

```text
TIMEOUT
```

Gateway rate limiting was tested by temporarily configuring a smaller request limit.

After the limit was exceeded, the API returned:

```http
HTTP 429 Too Many Requests
```

The normal configuration was restored after testing.

---

# Day 7 — Structured Output, Documentation and Final Testing

## Goals

- Add structured AI output
- Ensure structured requests are tracked
- Perform final API testing
- Prepare project documentation
- Prepare API examples and diagrams

## Work Completed

A structured AI endpoint was implemented:

```http
POST /api/v1/ai/structured
```

Example input:

```json
{
  "topic": "Dependency Injection in Spring Boot"
}
```

The expected output structure is:

```json
{
  "title": "...",
  "summary": "...",
  "keyPoints": [
    "...",
    "...",
    "..."
  ]
}
```

The AI response is parsed into a predefined Java DTO before being returned to the client.

Structured AI requests are also recorded in `ai_requests`.

Since structured requests do not require a conversation:

```text
conversation_id = NULL
```

is supported.

Project documentation was prepared, including:

- README
- API documentation
- API request/response examples
- Architecture diagram
- Database schema
- AI development worklog

## AI Assistance

AI was used to:

- Discuss the structured output approach
- Help diagnose Jackson package differences
- Review structured request tracking
- Review the final architecture
- Assist with documentation organization
- Assist with API testing examples
- Review the project against the challenge requirements

## Verification

The final API flow was tested using Postman:

```text
Register
   ↓
Login
   ↓
JWT
   ↓
Create Conversation
   ↓
AI Chat
   ↓
Structured AI
   ↓
Usage Statistics
```

The database was also inspected to verify that AI requests contain:

```text
user
model
timestamp
latency
input tokens
output tokens
status
```

---

# Key AI-Assisted Debugging Examples

AI assistance was particularly useful during debugging.

Some examples include:

## 1. Authentication Context

An authentication parameter configuration initially produced a null authentication value.

The controller was adjusted to use:

```text
JwtAuthenticationToken
```

directly.

---

## 2. Gemini Configuration

Gemini API integration required checking the correct API base URL, model configuration, and request structure.

The provider was tested independently before being integrated into the AI Gateway flow.

---

## 3. Missing Retry Configuration

A Spring test initially failed because the following property was missing:

```properties
gemini.max-retries
```

The test output was reviewed and the missing property was added.

---

## 4. Timeout Testing

The Gemini read timeout was temporarily reduced to force a timeout scenario.

This confirmed that:

```text
Retry
→ Retry
→ Retry
→ TIMEOUT
```

worked as expected.

The normal timeout configuration was restored afterward.

---

## 5. Structured Output and Jackson

The project uses Spring Boot 4 and its corresponding Jackson version.

An initial structured-output implementation used incompatible Jackson imports.

The imports were corrected to the Jackson packages available in the project.

The structured response could then be parsed successfully.

---

# How AI Was Used Responsibly

AI-generated code was not treated as automatically correct.

The development process followed this pattern:

```text
Requirement
    ↓
Discuss solution with AI
    ↓
Review suggested code
    ↓
Adapt to actual project structure
    ↓
Compile
    ↓
Run application
    ↓
Test with Postman
    ↓
Inspect logs/database
    ↓
Fix issues
    ↓
Commit working version
```

When AI suggestions did not match the actual project, the implementation was corrected based on:

- Compiler errors
- Spring Boot logs
- Maven test output
- Postman responses
- Database records
- Existing project code

AI was therefore used as a development assistant rather than as a replacement for testing and verification.

---

# Final Result

The completed AI Gateway provides:

- JWT authentication
- MySQL persistence
- Conversation management
- Gemini LLM integration
- Multi-provider abstraction
- Structured AI output
- AI request tracking
- Token tracking
- Latency tracking
- Usage statistics
- Global error handling
- Retry handling
- Timeout handling
- Logging
- Per-user rate limiting
- API documentation and examples

The project demonstrates how a backend service can provide a centralized and extensible gateway between client applications and external LLM providers.