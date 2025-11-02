# 🔧 TaskSphere Technical Documentation

## 🏗️ **Architecture Overview**

TaskSphere follows a microservices architecture pattern with event-driven communication and comprehensive monitoring.

### **System Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Client  │───▶│  Load Balancer  │───▶│  Spring Gateway │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
        ┌───────────────────────────────────────────────┼───────────────────────────────────────────────┐
        │                                               │                                               │
        ▼                                               ▼                                               ▼
┌─────────────────┐                            ┌─────────────────┐                            ┌─────────────────┐
│  Auth Service   │                            │ Project Service │                            │  Issue Service  │
└─────────────────┘                            └─────────────────┘                            └─────────────────┘
        │                                               │                                               │
        ├─────────────────────────────┐                 ├─────────────────────────────┐                 │
        ▼                             ▼                 ▼                             ▼                 ▼
┌─────────────────┐           ┌─────────────────┐ ┌─────────────────┐           ┌─────────────────┐ ┌─────────────────┐
│   MySQL RDS     │           │  Redis Cluster  │ │   MySQL RDS     │           │  Redis Pub/Sub  │ │      AWS S3     │
└─────────────────┘           └─────────────────┘ └─────────────────┘           └─────────────────┘ └─────────────────┘
                                       │                                                 │
                                       ▼                                                 ▼
                              ┌─────────────────┐                                ┌─────────────────┐
                              │   WebSocket     │                                │  Notification   │
                              │     STOMP       │                                │    Service      │
                              └─────────────────┘                                └─────────────────┘
```

### **Technology Stack Deep Dive**

#### **Backend Services**
- **Framework**: Spring Boot 3.5.7 with Java 21
- **Security**: Spring Security 6.x with JWT authentication
- **Data Access**: Spring Data JPA with Hibernate ORM
- **Caching**: Spring Cache with Redis integration
- **Messaging**: Spring WebSocket with STOMP protocol
- **Monitoring**: Spring Actuator with Micrometer

#### **Database Layer**
- **Primary Database**: MySQL 8.0 with InnoDB engine
- **Caching Layer**: Redis 7.0 with persistence enabled
- **Connection Pooling**: HikariCP with optimized configuration
- **Migration**: Flyway for database version control

#### **Infrastructure**
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: AWS ECS with Fargate
- **Load Balancing**: Application Load Balancer with SSL termination
- **Monitoring**: Prometheus + Grafana + CloudWatch

---

## 🔐 **Security Architecture**

### **Authentication Flow**
```
Client → Login Request → Auth Service → JWT Token → Client Stores Token
Client → API Request + JWT → Gateway → Validate Token → Route to Service
```

### **Security Layers**
1. **Transport Security**: HTTPS with TLS 1.3
2. **Application Security**: JWT with refresh tokens
3. **Authorization**: Role-Based Access Control (RBAC)
4. **Data Security**: Encryption at rest and in transit
5. **Infrastructure Security**: VPC with private subnets

### **Security Headers Implementation**
```java
// Comprehensive security headers
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

---

## 📊 **Database Design**

### **Entity Relationship Diagram**

```
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│    Users    │───▶│   UserProjects  │◀───│    Projects     │
│             │    │  (Many-to-Many) │    │                 │
│ - id (PK)   │    └─────────────────┘    │ - id (PK)       │
│ - email     │                           │ - name          │
│ - password  │                           │ - key           │
│ - role      │                           │ - description   │
│ - created   │                           │ - owner_id (FK) │
└─────────────┘                           └─────────────────┘
       │                                           │
       │                                           │
       ▼                                           ▼
┌─────────────┐                           ┌─────────────────┐
│  Comments   │                           │     Sprints     │
│             │                           │                 │
│ - id (PK)   │                           │ - id (PK)       │
│ - content   │                           │ - name          │
│ - author_id │                           │ - goal          │
│ - issue_id  │                           │ - start_date    │
│ - created   │                           │ - end_date      │
└─────────────┘                           │ - project_id    │
       ▲                                  └─────────────────┘
       │                                           │
┌─────────────┐                                   ▼
│   Issues    │                           ┌─────────────────┐
│             │                           │  SprintIssues   │
│ - id (PK)   │                           │  (Many-to-Many) │
│ - title     │                           └─────────────────┘
│ - description│
│ - status    │
│ - priority  │
│ - assignee  │
│ - project_id│
│ - created   │
└─────────────┘
       │
       ▼
┌─────────────┐
│ Attachments │
│             │
│ - id (PK)   │
│ - filename  │
│ - s3_key    │
│ - issue_id  │
│ - uploaded  │
└─────────────┘
```

### **Database Optimization**

#### **Indexing Strategy**
```sql
-- Performance indexes
CREATE INDEX idx_issues_project_status ON issues(project_id, status);
CREATE INDEX idx_issues_assignee ON issues(assignee_id);
CREATE INDEX idx_comments_issue ON comments(issue_id, created_at);
CREATE INDEX idx_activity_project ON activity_logs(project_id, created_at);

-- Composite indexes for complex queries
CREATE INDEX idx_issues_search ON issues(project_id, status, priority, assignee_id);
```

#### **Query Optimization**
- Batch processing for bulk operations
- Lazy loading for large collections
- Query result caching with Redis
- Connection pooling with HikariCP

---

## 🚀 **Performance Architecture**

### **Caching Strategy**

#### **Redis Cache Layers**
```
┌─────────────────┐
│  Application    │
│     Cache       │
│                 │
│ - User sessions │
│ - JWT tokens    │
│ - API responses │
└─────────────────┘
         │
┌─────────────────┐
│   Database      │
│     Cache       │
│                 │
│ - Query results │
│ - Entity cache  │
│ - Statistics    │
└─────────────────┘
```

#### **Cache Configuration**
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000ms  # 10 minutes
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "tasksphere:"
```

### **Database Performance**

#### **Connection Pool Optimization**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 600000
      connection-timeout: 30000
```

#### **JPA Performance Tuning**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc.batch_size: 20
        jdbc.fetch_size: 50
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
```

---

## 📡 **Real-Time Communication**

### **WebSocket Architecture**

```
Client WebSocket Connection
         │
         ▼
┌─────────────────┐
│  STOMP Broker   │
│                 │
│ - /topic/       │
│ - /queue/       │
│ - /app/         │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  Redis Pub/Sub  │
│                 │
│ - Project       │
│ - Issue updates │
│ - Notifications │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   Event Bus     │
│                 │
│ - Async         │
│ - Scalable      │
│ - Persistent    │
└─────────────────┘
```

### **Message Flow**
1. User action triggers event
2. Service publishes to Redis channel
3. Redis distributes to all subscribers
4. WebSocket sends update to connected clients
5. Client UI updates in real-time

---

## 📊 **Monitoring & Observability**

### **Metrics Collection**

#### **Custom Business Metrics**
```java
// Task management metrics
Counter tasksCreated = Counter.builder("tasksphere.tasks.created")
    .description("Number of tasks created")
    .register(meterRegistry);

Gauge activeUsers = Gauge.builder("tasksphere.users.active")
    .description("Number of active users")
    .register(meterRegistry, activeUsersGauge, AtomicInteger::doubleValue);

Timer apiResponseTime = Timer.builder("tasksphere.api.response.time")
    .description("API response time")
    .register(meterRegistry);
```

#### **System Metrics**
- JVM metrics (memory, GC, threads)
- HTTP request metrics (latency, throughput, errors)
- Database connection pool metrics
- Cache hit/miss ratios
- Custom application metrics

### **Alerting Rules**

#### **Critical Alerts**
```yaml
groups:
  - name: tasksphere-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          
      - alert: DatabaseConnectionPoolExhaustion
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Database connection pool nearly exhausted"
```

---

## 🔧 **Configuration Management**

### **Environment-Specific Configuration**

#### **Development (application-dev.yml)**
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/tasksphere_dev
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop
logging:
  level:
    com.tasksphere: DEBUG
```

#### **Production (application-prod.yml)**
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
logging:
  level:
    com.tasksphere: INFO
```

### **Secrets Management**
- AWS Secrets Manager for production credentials
- Environment variables for configuration
- Encrypted properties for sensitive data
- IAM roles for service authentication

---

## 🐳 **Deployment Architecture**

### **Docker Strategy**
- Multi-stage builds for optimized images
- Non-root user for security
- Health checks for container orchestration
- Resource limits and requests

### **AWS Deployment**

#### **ECS Task Definition**
```json
{
  "family": "tasksphere-backend",
  "requiresCompatibilities": ["FARGATE"],
  "networkMode": "awsvpc",
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "tasksphere-backend",
      "image": "123456789.dkr.ecr.us-east-1.amazonaws.com/tasksphere:latest",
      "portMappings": [
        {
          "containerPort": 8081,
          "protocol": "tcp"
        }
      ],
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8081/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3
      }
    }
  ]
}
```

---

## 🧪 **Testing Strategy**

### **Test Pyramid**

```
                    ┌─────────────┐
                    │   E2E Tests │ (5%)
                    └─────────────┘
                ┌─────────────────────┐
                │ Integration Tests   │ (20%)
                └─────────────────────┘
        ┌─────────────────────────────────┐
        │         Unit Tests              │ (75%)
        └─────────────────────────────────┘
```

### **Testing Technologies**
- **Unit Testing**: JUnit 5, Mockito, TestContainers
- **Integration Testing**: Spring Boot Test, WireMock
- **Performance Testing**: JMeter, Apache Bench
- **Security Testing**: OWASP ZAP, Snyk

### **Test Coverage Goals**
- Unit Tests: 85%+ line coverage
- Integration Tests: All API endpoints
- Performance Tests: Load testing scenarios
- Security Tests: OWASP Top 10 validation

---

## 🔄 **CI/CD Pipeline**

### **Pipeline Stages**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Source    │───▶│   Build     │───▶│    Test     │───▶│   Security  │
│             │    │             │    │             │    │    Scan     │
│ - Git push  │    │ - Compile   │    │ - Unit      │    │ - SAST      │
│ - PR merge  │    │ - Package   │    │ - Integration│    │ - DAST      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                                                        │
        ▼                                                        ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Deploy    │◀───│  Performance│◀───│   Deploy    │◀───│   Package   │
│ Production  │    │   Testing   │    │   Staging   │    │   Docker    │
│             │    │             │    │             │    │   Image     │
│ - ECS       │    │ - Load test │    │ - ECS       │    │ - ECR Push  │
│ - Health    │    │ - Monitoring│    │ - Smoke test│    │ - Scan      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### **Deployment Strategies**
- **Blue-Green**: Zero-downtime deployments
- **Rolling Updates**: Gradual rollout with health checks
- **Canary Releases**: Gradual traffic shifting
- **Feature Flags**: Runtime feature toggling

---

## 📚 **API Documentation**

### **OpenAPI Specification**
Complete API documentation with:
- Request/response schemas
- Authentication requirements
- Example requests and responses
- Error codes and messages
- Rate limiting information

### **API Versioning Strategy**
- URL-based versioning: `/api/v1/`, `/api/v2/`
- Backward compatibility for 2 major versions
- Deprecation notices with migration guides
- Semantic versioning for API changes

---

## 🔍 **Troubleshooting Guide**

### **Common Issues**

#### **Database Connection Issues**
```bash
# Check connection pool status
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active

# Check database connectivity
curl http://localhost:8081/actuator/health/db
```

#### **Redis Connection Issues**
```bash
# Check Redis connectivity
curl http://localhost:8081/actuator/health/redis

# Monitor Redis commands
redis-cli monitor
```

#### **Performance Issues**
```bash
# Check JVM metrics
curl http://localhost:8081/actuator/metrics/jvm.memory.used

# Check API response times
curl http://localhost:8081/actuator/metrics/http.server.requests
```

---

## 📞 **Support & Maintenance**

### **Monitoring Dashboards**
- **Application**: Grafana dashboard for business metrics
- **Infrastructure**: CloudWatch for AWS resources
- **Logs**: ELK stack for centralized logging
- **Alerts**: PagerDuty integration for critical issues

### **Backup & Recovery**
- **Database**: Automated daily backups with 30-day retention
- **Application**: Blue-green deployment for quick rollback
- **Configuration**: Git-based configuration management
- **Disaster Recovery**: Multi-AZ deployment with failover

---

**📧 For technical questions or issues, contact the development team at manohar.pagadala@example.com**