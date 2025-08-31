# Auth Service

A robust authentication and authorization microservice built with Java 21, providing secure user management, JWT token handling, and role-based access control.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Authentication Flow](#authentication-flow)
- [Security Features](#security-features)
- [Database Schema](#database-schema)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Contributing](#contributing)

## Overview

The Auth Service is a standalone microservice designed to handle all authentication and authorization concerns for your application ecosystem. It provides secure user registration, login, token management, and role-based access control with modern security practices.

## Features

- ✅ Reactive User Registration and Authentication
- ✅ JWT Token Generation and Validation with JJWT
- ✅ Email-based User Login System
- ✅ Role-Based Access Control (RBAC)
- ✅ Reactive Kafka Event Publishing
- ✅ Integrated Logging Service with WebClient
- ✅ Reactive Programming Model with Spring WebFlux
- ✅ Token Expiration (24 hours default)
- ✅ Claims-based Authentication
- ✅ Error Handling with Custom Exceptions
- ✅ Async Message Processing
- ✅ External Logging Service Integration

## Technology Stack

- **Java 21** - Latest LTS version with modern features
- **Spring Boot 3.x** - Reactive web application framework
- **Spring WebFlux** - Reactive programming model
- **Spring Security 6.x** - Security framework
- **JWT (JJWT 0.11.5)** - Token-based authentication
- **Apache Kafka** - Event streaming and messaging
- **Reactive Kafka** - Reactive Kafka integration
- **Lombok** - Code generation and boilerplate reduction
- **Gradle** - Dependency management and build tool
- **WebClient** - Reactive HTTP client for logging service

### Build Configuration

```gradle
// build.gradle
plugins {
    id 'org.springframework.boot' version '3.x.x'
    id 'io.spring.dependency-management' version '1.x.x'
    id 'java'
}

java {
    sourceCompatibility = '21'
    targetCompatibility = '21'
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## Installation

### Prerequisites

- Java 21 or higher
- Gradle 7.0+
- Apache Kafka 2.8+
- External Logging Service
- Database (for PeopleService)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd auth_service
   ```

2. **Set up Kafka**
   ```bash
   # Start Kafka (using Docker)
   docker run -d --name kafka \
     -p 9092:9092 \
     -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
     -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
     confluentinc/cp-kafka:latest
   ```

3. **Configure environment variables**
   ```bash
   export JWT_SECRET=your-secret-key-here
   export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   export LOGGING_SERVICE_URL=http://localhost:8081
   export PEOPLE_SERVICE_DATABASE_URL=jdbc:postgresql://localhost:5432/people_db
   ```

4. **Build and run**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

### Docker Deployment

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY build/libs/auth-service-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build the application
./gradlew clean build

# Build Docker image
docker build -t auth-service:latest .

# Run with Docker Compose
docker-compose up -d
```

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `JWT_SECRET` | JWT signing secret key | - | ✅ |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | localhost:9092 | ✅ |
| `LOGGING_SERVICE_URL` | External logging service URL | - | ✅ |
| `APP_NAME` | Application name for logging | auth-service | ❌ |
| `PEOPLE_SERVICE_DATABASE_URL` | Database connection URL | - | ✅ |

### Application Properties

```yaml
# application.yml
server:
  port: 8080

spring:
  webflux:
    base-path: /api/v1
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      retries: 3
      acks: all
    
  security:
    enabled: true

# JWT Configuration
jwt:
  secret: ${JWT_SECRET}
  expiration-days: 1

# Logging Service
logging:
  service:
    url: ${LOGGING_SERVICE_URL}
    
# Application Info
app:
  name: ${APP_NAME:auth-service}
```

### Kafka Topics

The service publishes to the following Kafka topics:

- **create** - User creation and login events
  - Message Format: JWT token string
  - Used for: User activity tracking, audit logs, downstream service notifications

### External Services

#### Logging Service Integration
- **Method:** HTTP PUT requests via WebClient
- **Endpoint:** `/write`
- **Parameters:**
  - `type` - Log level (debug, info, warn, error)
  - `message` - Log message with caller context
- **Format:** `{APP_NAME}[{methodName}] {message}`

## API Endpoints

### Authentication Endpoints

#### POST /people
Register a new user and generate JWT token.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER"]
}
```

**Response:**
```text
Generated JWT token string
```

**Flow:**
1. Creates user via PeopleService
2. Generates JWT token with user claims
3. Logs token generation (debug level)
4. Extracts user information from token
5. Publishes "create" event to Kafka
6. Returns token string

#### POST /people/log
Authenticate existing user with email and password.

**Request Parameters:**
- `email` (String) - User's email address
- `password` (String) - User's password

**Response:**
```text
Generated JWT token string
```

**Flow:**
1. Validates user credentials via email/password
2. Generates JWT token with user claims
3. Extracts user information from token
4. Publishes "create" event to Kafka
5. Returns token string

### JWT Token Structure

**Token Claims:**
```json
{
  "sub": "user@example.com",
  "roles": ["USER", "ADMIN"],
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Token Configuration:**
- **Algorithm:** HS256
- **Expiration:** 24 hours (1 day)
- **Subject:** User's email address
- **Custom Claims:** User roles array

## Architecture Components

### Core Services

#### PeopleService
Handles user management operations including user creation and authentication.

#### JwtService
**Key Methods:**
- `generateToken(PeopleBoundary user)` - Creates JWT token with user claims
- `extractUser(String token)` - Parses token and extracts user information

**Token Configuration:**
```java
// Token expires in 24 hours
.setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
.signWith(key, SignatureAlgorithm.HS256)
```

#### KafkaReactiveProducer
Publishes events to Kafka topics reactively.

**Key Methods:**
- `sendMessage(String topic, String message)` - Sends message to specified Kafka topic

#### Logger Service
Integrates with external logging service via WebClient.

**Key Methods:**
- `printer(String level, String message)` - Sends logs to external service with caller context

### Data Models

#### PeopleBoundary
```java
public class PeopleBoundary {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private List<String> roles;
    // getters and setters
}
```

### Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.kafka:spring-kafka'
    
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## Security Features

### Password Security
- BCrypt hashing with configurable rounds
- Password strength validation
- Password history tracking (prevents reuse)
- Secure password reset with time-limited tokens

### Account Protection
- Account lockout after failed login attempts
- Rate limiting on authentication endpoints
- Email verification for new accounts
- Suspicious activity detection

### Token Security
- JWT tokens with configurable expiration
- Secure refresh token rotation
- Token blacklisting for logout
- CSRF protection

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- XSS protection headers
- Encrypted sensitive data storage

### Logging
The service provides structured logging with different levels:
- `INFO` - Normal operations
- `WARN` - Authentication failures, rate limiting
- `ERROR` - System errors, database issues
- `DEBUG` - Detailed request/response information


### Development Guidelines

- Follow Java coding standards
- Write unit tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR
- Use conventional commit messages


---

**Version:** 1.0.0  
**Last Updated:** August 2025
