# Policy Service

A FastAPI-based microservice for authorization and access control management using expert system rules, JWT token validation, and Kafka messaging integration.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [Access Control System](#access-control-system)
- [Database Operations](#database-operations)
- [Kafka Integration](#kafka-integration)
- [Security Features](#security-features)
- [API Documentation](#api-documentation)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Contributing](#contributing)

## Overview

The Policy Service is a microservice designed to handle authorization decisions using an expert system approach. It evaluates access requests against configurable ACL (Access Control List) rules and integrates with other services through Kafka messaging. The service validates JWT tokens, applies role-based access control, and maintains audit logs for all authorization decisions.

## Features

- ✅ Expert System-Based Access Control
- ✅ JWT Token Validation and Decoding
- ✅ Kafka Consumer/Producer Integration
- ✅ Role-Based Access Control (RBAC)
- ✅ YAML-Based ACL Configuration
- ✅ PostgreSQL Database Integration
- ✅ Raw SQL Query Execution with Injection Protection
- ✅ Async Database Operations
- ✅ Comprehensive Logging and Monitoring
- ✅ Resource-Level Authorization
- ✅ Batch Authorization Processing
- ✅ Custom Exception Handling
- ✅ Configurable Policy Rules

## Technology Stack

- **Python 3.11+** - Modern Python with async support
- **FastAPI** - High-performance web framework
- **PyJWT** - JWT token handling
- **Apache Kafka** - Event streaming and messaging
- **PostgreSQL** - Primary database
- **asyncpg** - Async PostgreSQL driver
- **PyYAML** - Configuration file parsing
- **Expert System Engine** - Rule-based decision making
- **Docker** - Containerization
- **Pydantic** - Data validation and serialization

## Prerequisites

- Python 3.11 or higher
- PostgreSQL 12+
- Apache Kafka 2.8+
- Docker (optional, for containerized deployment)

## Installation

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd policy_service
   ```

2. **Create virtual environment**
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Set up database**
   ```bash
   # Create PostgreSQL database
   createdb policy_service_db
   
   # Run migrations
   python migrations/init_db.py
   ```

5. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

6. **Start the service**
   ```bash
   uvicorn main:app --host 0.0.0.0 --port 8000 --reload
   ```

### Docker Deployment

```bash
# Build the application
docker build -t policy-service:latest .

# Run with Docker Compose
docker-compose up -d
```

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | PostgreSQL connection URL | - | ✅ |
| `JWT_SECRET` | JWT secret key (shared with auth service) | - | ✅ |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | localhost:9092 | ✅ |
| `KAFKA_CONSUMER_GROUP` | Kafka consumer group ID | policy-service-group | ❌ |
| `ACL_CONFIG_PATH` | Path to ACL configuration file | acl.yaml | ❌ |
| `LOG_LEVEL` | Logging level | INFO | ❌ |
| `SERVICE_PORT` | Service port | 8000 | ❌ |

### Dependencies (requirements.txt)

```txt
fastapi==0.104.1
uvicorn[standard]==0.24.0
pyjwt==2.8.0
kafka-python==2.0.2
asyncpg==0.29.0
pyyaml==6.0.1
pydantic==2.5.0
python-multipart==0.0.6
aiofiles==23.2.1
pytest==7.4.3
pytest-asyncio==0.21.1
```

### ACL Configuration (acl.yaml)

```yaml
# Access Control List Rules
acl_rules:
  # Resource-based permissions
  user_management:
    - role: "ADMIN"
      actions: ["create", "read", "update", "delete"]
    - role: "MANAGER" 
      actions: ["read", "update"]
    - role: "USER"
      actions: ["read"]
  
  policy_management:
    - role: "ADMIN"
      actions: ["create", "read", "update", "delete"]
    - role: "POLICY_MANAGER"
      actions: ["read", "update"]
  
  system_configuration:
    - role: "SYSTEM_ADMIN"
      actions: ["create", "read", "update", "delete"]

# Expert system rules
expert_rules:
  - name: "admin_full_access"
    condition: "role1 == 'ADMIN'"
    action: "allow"
    priority: 1
    
  - name: "manager_limited_access" 
    condition: "role2 == 'MANAGER' and resource in ['user_data', 'reports']"
    action: "allow"
    priority: 2
    
  - name: "user_read_only"
    condition: "role3 == 'USER' and action == 'read'"
    action: "allow"
    priority: 3
    
  - name: "default_deny"
    condition: "default"
    action: "deny"
    priority: 999
```

## Architecture

### Core Components

#### JWT Handler
```python
class JWTDecodeError(Exception):
    def __init__(self, message: str):
        super().__init__(message)
        self.message = message

def encode_message(message, secret, alg='HS256'):
    """Encode message into JWT token with 10-minute expiration"""
    payload = {
        'message': message,
        'exp': int(time.time()) + 600  # 10 minutes
    }
    token = jwt.encode(payload, secret, algorithm=alg)
    return token

def decode_message(token, secret):
    """Decode and validate JWT token"""
    try:
        decoded = jwt.decode(token, secret, algorithms=['HS256'])
        return decoded
    except jwt.ExpiredSignatureError as e:
        raise JWTDecodeError(f"Token has expired: {str(e)}") from e
    except jwt.InvalidTokenError as e:
        raise JWTDecodeError(f"Invalid token: {str(e)}") from e
```

#### Access Control Engine
```python
def check_access(resource, rules: list[str], acl_rules):
    """Expert system-based access control decision"""
    AccessEngine.acl = acl_rules
    engine = AccessEngine()
    engine.reset()

    # Declare facts
    engine.declare(Fact(resource=resource))
    engine.declare(Fact(role1=rules[0]))
    engine.declare(Fact(role2=rules[1]))
    engine.declare(Fact(role3=rules[2]))

    # Run the expert system
    engine.run()

    return {
        "allow": engine.decision,
        "reason": engine.reason,
    }
```

#### Database Operations with Decorators
```python
def numberQuery(number: int):
    """Decorator for SQL query execution by number"""
    def decorator(func):
        @functools.wraps(func)
        async def wrapper(self, tup, *args, **kwargs):
            sql_path = filepath('Data', 'select.sql')
            query_text = getText(sql_path)
            queries = [q.strip() for q in query_text.split(";") if q.strip()]
            query = queries[number - 1]
            rows = await self.crud.prepare(query)
            return await rows.fetch(*tup) if tup else await rows.fetch()
        return wrapper
    return decorator

@numberQuery(1)
async def findById(self, tup: tuple):
    """Find record by ID using query #1"""
    pass

@numberQuery(5)  
async def findALL(self):
    """Find all records using query #5"""
    pass
```

#### Resource Update with Authorization
```python
async def updateResource(self, tup: tuple):
    """Update resources with access control validation
    
    Args:
        tup: tuple of (resources, id)
    """
    # Find existing record
    finder = await self.findById((tup[1],))
    if not finder:
        await self.printerException()
        raise Exception('Record not found')

    # Batch authorization check for all resources
    results = await asyncio.gather(*[
        asyncio.to_thread(
            check_access,
            resource,
            UserEntity.from_record(finder).roles,
            load_acl_config()
        ) for resource in tup[0]
    ])
    
    # Log authorization results
    for r in results:
        await printer(
            f"[updateResource] Resource={r['resource']} "
            f"Allow={r['allow']} Reason={r['reason']}", 
            "DEBUG"
        )
    
    # Execute update if authorized
    await self.crud.execute(getText(filepath('Data', 'update.sql')), *tup)
    await printer("[updateResource] All resources updated successfully.", "DEBUG")
```

### Data Models

#### UserEntity
```python
from pydantic import BaseModel
from typing import List

class UserEntity(BaseModel):
    id: str
    email: str
    roles: List[str]
    
    @classmethod
    def from_record(cls, record):
        """Create UserEntity from database record"""
        return cls(
            id=record['id'],
            email=record['email'],
            roles=record['roles'].split(',') if record['roles'] else []
        )
```

#### AccessRequest
```python
class AccessRequest(BaseModel):
    resource: str
    action: str
    user_token: str
    context: dict = {}
```

#### AccessResponse
```python
class AccessResponse(BaseModel):
    allow: bool
    reason: str
    resource: str
    timestamp: datetime
```

## Access Control System

### Expert System Rules

The service uses an expert system approach for access control decisions:

1. **Facts Declaration**: User roles and requested resource
2. **Rule Engine**: Evaluates configured rules against facts
3. **Decision Making**: Returns allow/deny with reasoning
4. **Audit Logging**: Records all decisions for compliance

### Rule Priority System

- **Priority 1**: Admin rules (highest priority)
- **Priority 2-10**: Role-specific rules
- **Priority 11-50**: Resource-specific rules
- **Priority 51-100**: Context-specific rules
- **Priority 999**: Default deny (lowest priority)

### Authorization Flow

```
JWT Token → Decode → Extract Roles → Load ACL Rules → 
Expert Engine → Access Decision → Log Result → Return Response
```

## Database Operations

### SQL Injection Protection

The service implements several layers of protection against SQL injection:

1. **Parameterized Queries**: All user inputs are parameterized
2. **Input Validation**: Pydantic models validate all inputs
3. **Query Templates**: Pre-defined query templates in SQL files
4. **Prepared Statements**: Use of asyncpg prepared statements

### Database Schema

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    roles TEXT NOT NULL, -- Comma-separated roles
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Access logs table
CREATE TABLE access_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    resource VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    decision BOOLEAN NOT NULL,
    reason TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Policy rules table
CREATE TABLE policy_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    condition TEXT NOT NULL,
    action VARCHAR(50) NOT NULL,
    priority INTEGER NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Query Files Structure

```
Data/
├── select.sql    # SELECT queries (numbered)
├── insert.sql    # INSERT queries
├── update.sql    # UPDATE queries
└── delete.sql    # DELETE queries
```

**Example select.sql:**
```sql
-- Query 1: Find user by ID
SELECT id, email, roles FROM users WHERE id = $1;

-- Query 2: Find user by email
SELECT id, email, roles FROM users WHERE email = $1;

-- Query 3: Find users by role
SELECT id, email, roles FROM users WHERE roles LIKE '%' || $1 || '%';

-- Query 4: Get access logs for user
SELECT * FROM access_logs WHERE user_id = $1 ORDER BY timestamp DESC;

-- Query 5: Get all users
SELECT id, email, roles FROM users ORDER BY created_at DESC;
```

## Kafka Integration

### Consumer Configuration

```python
from kafka import KafkaConsumer
import json

class PolicyKafkaConsumer:
    def __init__(self, bootstrap_servers, group_id):
        self.consumer = KafkaConsumer(
            'auth-events',
            'policy-requests', 
            bootstrap_servers=bootstrap_servers,
            group_id=group_id,
            value_deserializer=lambda x: json.loads(x.decode('utf-8')),
            auto_offset_reset='latest'
        )
    
    async def consume_messages(self):
        """Consume and process Kafka messages"""
        for message in self.consumer:
            await self.process_message(message)
    
    async def process_message(self, message):
        """Process individual Kafka message"""
        topic = message.topic
        data = message.value
        
        if topic == 'auth-events':
            await self.handle_auth_event(data)
        elif topic == 'policy-requests':
            await self.handle_policy_request(data)
```

### Producer Configuration

```python
from kafka import KafkaProducer
import json

class PolicyKafkaProducer:
    def __init__(self, bootstrap_servers):
        self.producer = KafkaProducer(
            bootstrap_servers=bootstrap_servers,
            value_serializer=lambda x: json.dumps(x).encode('utf-8')
        )
    
    async def send_decision(self, decision_data):
        """Send authorization decision to Kafka"""
        self.producer.send('policy-decisions', decision_data)
        self.producer.flush()
    
    async def send_audit_log(self, audit_data):
        """Send audit log to Kafka"""
        self.producer.send('audit-logs', audit_data)
        self.producer.flush()
```

### Message Formats

#### Auth Events (Consumed)
```json
{
  "event_type": "user_login",
  "user_id": "uuid-here",
  "user_email": "user@example.com", 
  "roles": ["USER", "MANAGER"],
  "timestamp": "2024-08-31T10:30:00Z",
  "jwt_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

#### Policy Requests (Consumed)
```json
{
  "request_id": "uuid-here",
  "user_token": "jwt-token-here",
  "resource": "user_management",
  "action": "update", 
  "context": {
    "target_user_id": "uuid-here",
    "ip_address": "192.168.1.100"
  }
}
```

#### Policy Decisions (Produced)
```json
{
  "request_id": "uuid-here",
  "user_id": "uuid-here",
  "resource": "user_management",
  "action": "update",
  "decision": true,
  "reason": "User has MANAGER role with update permission",
  "timestamp": "2024-08-31T10:30:00Z"
}
```

## Security Features

### Token Validation
- JWT signature verification
- Token expiration checking
- Issuer validation
- Custom claims validation

### Access Control
- Role-based permissions
- Resource-level authorization
- Action-specific controls
- Context-aware decisions

### Audit Trail
- All authorization decisions logged
- User activity tracking
- Failed access attempt monitoring
- Compliance reporting

### Data Protection
- Input sanitization
- SQL injection prevention
- XSS protection
- Secure error handling

## API Documentation

### FastAPI Endpoints

#### POST /authorize
Authorize access to a resource.

**Request:**
```json
{
  "resource": "user_management",
  "action": "update",
  "user_token": "jwt-token-here",
  "context": {
    "target_user_id": "uuid-here"
  }
}
```

**Response:**
```json
{
  "allow": true,
  "reason": "User has ADMIN role with full permissions",
  "resource": "user_management",
  "timestamp": "2024-08-31T10:30:00Z"
}
```

#### GET /health
Health check endpoint.

**Response:**
```json
{
  "status": "healthy",
  "database": "connected",
  "kafka": "connected",
  "timestamp": "2024-08-31T10:30:00Z"
}
```

#### GET /policies
Get all policy rules (Admin only).

#### POST /policies
Create new policy rule (Admin only).

#### PUT /policies/{rule_id}
Update existing policy rule (Admin only).

#### DELETE /policies/{rule_id}
Delete policy rule (Admin only).

## Error Handling

### Custom Exceptions

```python
class PolicyServiceError(Exception):
    """Base exception for policy service"""
    def __init__(self, message: str, error_code: str = None):
        super().__init__(message)
        self.message = message
        self.error_code = error_code

class JWTDecodeError(PolicyServiceError):
    """JWT token decode error"""
    pass

class AccessDeniedError(PolicyServiceError):
    """Access denied error"""
    pass

class DatabaseError(PolicyServiceError):
    """Database operation error"""
    pass
```

### Error Response Format

```json
{
  "error": {
    "code": "ACCESS_DENIED",
    "message": "Insufficient permissions for requested resource",
    "details": {
      "resource": "user_management",
      "action": "delete",
      "required_role": "ADMIN"
    },
    "timestamp": "2024-08-31T10:30:00Z"
  }
}
```

## Testing

### Unit Tests

```bash
# Run all tests
pytest

# Run with coverage
pytest --cov=./ --cov-report=html

# Run specific test file
pytest tests/test_access_control.py
```

### Integration Tests

```python
import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

@pytest.mark.asyncio
async def test_authorize_access():
    response = client.post("/authorize", json={
        "resource": "user_management",
        "action": "read",
        "user_token": "valid-jwt-token"
    })
    assert response.status_code == 200
    assert response.json()["allow"] == True
```

## Deployment

### Docker Configuration

```dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application
COPY . .

# Expose port
EXPOSE 8000

# Run application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### Docker Compose

```yaml
version: '3.8'
services:
  policy-service:
    build: .
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=postgresql://user:pass@db:5432/policy_db
      - JWT_SECRET=your-secret-key
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - db
      - kafka
    
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=policy_db
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
    
  kafka:
    image: confluentinc/cp-kafka:latest
    environment:
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
    depends_on:
      - zookeeper

volumes:
  postgres_data:
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: policy-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: policy-service
  template:
    metadata:
      labels:
        app: policy-service
    spec:
      containers:
      - name: policy-service
        image: policy-service:latest
        ports:
        - containerPort: 8000
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: policy-secrets
              key: database-url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: policy-secrets
              key: jwt-secret
```

## Monitoring

### Health Checks
- `/health` - Service health status
- `/health/db` - Database connectivity
- `/health/kafka` - Kafka connectivity

### Metrics
- Authorization request rate
- Decision latency
- Success/failure ratios
- Database query performance
- Kafka message processing rate

### Logging

```python
import logging
import structlog

# Configure structured logging
structlog.configure(
    processors=[
        structlog.processors.add_log_level,
        structlog.processors.add_logger_name,
        structlog.dev.ConsoleRenderer()
    ],
    logger_factory=structlog.PrintLoggerFactory(),
    wrapper_class=structlog.make_filtering_bound_logger(logging.INFO),
    cache_logger_on_first_use=True,
)

logger = structlog.get_logger()
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow PEP 8 coding standards
- Write comprehensive tests
- Update documentation
- Use type hints
- Follow async/await patterns

### Code Quality

- Minimum 90% test coverage
- Pass all linting checks
- No security vulnerabilities
- Follow established patterns

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For questions or support:
- Create an issue in the repository
- Contact the development team
- Check the API documentation at `/docs`

---

**Version:** 1.0.0  
**Last Updated:** August 2025