# Auth Service

Authentication and authorization service for the Marketplace Platform.

## Overview

The Auth Service is responsible for:

* User registration
* User authentication
* Password hashing
* JWT token generation
* JWT token validation
* Role management
* User identity propagation across microservices

This service acts as the central identity provider for the platform.

---

## Architecture

```text
Client
  |
  v
Auth Service
  |
  +--> DynamoDB (Users)
  |
  +--> AWS Secrets Manager (RSA Keys)
  |
  +--> JWT Tokens (RS256)
```

### Authentication Flow

```text
Register
  |
  v
User stored in DynamoDB
  |
  v
Password hashed with BCrypt
```

```text
Login
  |
  v
Credentials validated
  |
  v
JWT generated (RS256)
  |
  v
Token returned to client
```

```text
Authenticated Request
  |
  v
JWT validated
  |
  v
SecurityContext populated
  |
  v
Protected endpoint executed
```

---

## Technologies

* Java 21
* Spring Boot 3
* Spring Security
* DynamoDB
* AWS Secrets Manager
* JWT (RS256)
* Docker
* OpenAPI Generator
* Lombok

---

## Security

### Password Storage

Passwords are never stored in plain text.

The service uses:

```text
BCrypt
```

for password hashing.

### JWT

Tokens are signed using:

```text
RS256
```

The private and public keys are stored in:

```text
AWS Secrets Manager
```

and loaded during application startup.

### Authorization

Public endpoints:

```text
POST /auth/register
POST /auth/login
GET  /actuator/health
```

Protected endpoints require a valid JWT.

---

## User Model

```text
USER
├── userId
├── email
├── passwordHash
├── role
└── createdAt
```

Supported roles:

```text
CUSTOMER
ADMIN
```

Public registration always creates:

```text
CUSTOMER
```

users.

---

## API

### Register

```http
POST /auth/register
```

### Login

```http
POST /auth/login
```

### Current User

```http
GET /auth/me
Authorization: Bearer <token>
```

---

## Engineering Quality

The project follows a test-first mindset for critical authentication and security flows.

### Automated Testing

* Unit tests with JUnit 5
* Mockito-based dependency isolation
* Security flow validation
* JWT generation and validation tests
* Exception handling tests
* Password validation tests

### Quality Gates

| Metric            | Result |
| ----------------- | ------ |
| Line Coverage     | 100%   |
| Mutation Coverage | 100%   |
| Test Strength     | 100%   |

Mutation testing is performed using PIT to ensure tests validate behavior rather than simply execute code paths.

---

## Running Locally

### Prerequisites

* Java 21
* Maven
* AWS credentials configured
* DynamoDB table created
* RSA keys stored in AWS Secrets Manager

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

---

## Docker

Build image:

```bash
docker build -t auth-service .
```

Run container:

```bash
docker run -p 8085:8085 auth-service
```

---

## Marketplace Platform

This service is part of the Marketplace Platform microservices ecosystem:

* Auth Service
* Order Service
* Payment Service
* Inventory Service
* Notification Service
* Admin Service
* Event Driven Core

The Auth Service provides identity and access management for all platform services.