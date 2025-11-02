# 📊 TaskSphere Monitoring Setup Guide

## 🎯 **Overview**

This guide provides comprehensive instructions for setting up monitoring, logging, and alerting for TaskSphere across different environments.

---

## 🩺 **Health Monitoring**

### **Spring Boot Actuator Endpoints**

TaskSphere exposes several health and monitoring endpoints:

| Endpoint | Purpose | Access Level |
|----------|---------|--------------|
| `/actuator/health` | Overall application health | Public |
| `/actuator/health/db` | Database connectivity | Admin |
| `/actuator/health/redis` | Redis connectivity | Admin |
| `/actuator/metrics` | Application metrics | Admin |
| `/actuator/prometheus` | Prometheus metrics | Internal |
| `/actuator/info` | Application information | Public |

### **Custom Health Indicators**

```java
// Database Health Check
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5-second timeout
            return isValid ? Health.up().build() : Health.down().build();
        } catch (SQLException e) {
            return Health.down().withException(e).build();
        }
    }
}
```

### **Health Check Configuration**

```yaml
management:
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    db:
      enabled: true
    redis:
      enabled: true
    diskspace:
      enabled: true
```

---

## 📊 **Prometheus Setup**

### **Local Prometheus Setup**

```bash
# Using Docker
docker run -d \
  --name prometheus \
  -p 9090:9090 \
  -v $(pwd)/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:latest
```

### **Prometheus Configuration**

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

rule_files:
  - "rules/*.yml"

scrape_configs:
  # TaskSphere Application
  - job_name: 'tasksphere-app'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
    static_configs:
      - targets: ['localhost:8081']
    metric_relabel_configs:
      - source_labels: [__name__]
        regex: 'jvm_.*|http_.*|system_.*|hikaricp_.*|cache_.*|tasksphere_.*'
        action: keep

  # System Metrics (Node Exporter)
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['localhost:9100']

  # Database Metrics (MySQL Exporter)
  - job_name: 'mysql-exporter'
    static_configs:
      - targets: ['localhost:9104']

  # Redis Metrics
  - job_name: 'redis-exporter'
    static_configs:
      - targets: ['localhost:9121']
```

### **Custom Business Metrics**

```java
// Task Management Metrics
@Service
public class MetricsService {
    
    private final Counter tasksCreated;
    private final Counter tasksCompleted;
    private final Gauge activeUsers;
    private final Timer apiResponseTime;
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.tasksCreated = Counter.builder("tasksphere_tasks_created_total")
            .description("Total number of tasks created")
            .register(meterRegistry);
            
        this.tasksCompleted = Counter.builder("tasksphere_tasks_completed_total")
            .description("Total number of tasks completed")
            .register(meterRegistry);
            
        this.activeUsers = Gauge.builder("tasksphere_users_active")
            .description("Number of currently active users")
            .register(meterRegistry, this, MetricsService::getActiveUserCount);
            
        this.apiResponseTime = Timer.builder("tasksphere_api_response_time")
            .description("API response time")
            .register(meterRegistry);
    }
    
    public void recordTaskCreated() {
        tasksCreated.increment();
    }
    
    public void recordTaskCompleted() {
        tasksCompleted.increment();
    }
    
    private double getActiveUserCount() {
        // Implementation to get active user count
        return activeUserService.getActiveUserCount();
    }
}
```

---

## 📈 **Grafana Dashboard Setup**

### **Grafana Installation**

```bash
# Using Docker
docker run -d \
  --name grafana \
  -p 3000:3000 \
  -e "GF_SECURITY_ADMIN_PASSWORD=admin123" \
  -v grafana-storage:/var/lib/grafana \
  grafana/grafana:latest
```

### **Prometheus Data Source Configuration**

```yaml
# grafana/datasources/prometheus.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: false
```

### **TaskSphere Dashboard Configuration**

```json
{
  "dashboard": {
    "id": null,
    "title": "TaskSphere Application Dashboard",
    "tags": ["tasksphere", "spring-boot"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Application Status",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"tasksphere-app\"}",
            "legendFormat": "Application Status"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "mappings": [
              {
                "options": {
                  "0": {
                    "text": "DOWN",
                    "color": "red"
                  },
                  "1": {
                    "text": "UP",
                    "color": "green"
                  }
                },
                "type": "value"
              }
            ]
          }
        }
      },
      {
        "id": 2,
        "title": "HTTP Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{job=\"tasksphere-app\"}[5m])",
            "legendFormat": "Requests/sec"
          }
        ]
      },
      {
        "id": 3,
        "title": "Response Time (95th percentile)",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job=\"tasksphere-app\"}[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "id": 4,
        "title": "JVM Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{job=\"tasksphere-app\"}",
            "legendFormat": "{{area}}"
          }
        ]
      },
      {
        "id": 5,
        "title": "Database Connection Pool",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active{job=\"tasksphere-app\"}",
            "legendFormat": "Active Connections"
          },
          {
            "expr": "hikaricp_connections_max{job=\"tasksphere-app\"}",
            "legendFormat": "Max Connections"
          }
        ]
      },
      {
        "id": 6,
        "title": "Cache Hit Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(cache_gets_total{result=\"hit\",job=\"tasksphere-app\"}[5m]) / rate(cache_gets_total{job=\"tasksphere-app\"}[5m]) * 100",
            "legendFormat": "Hit Rate %"
          }
        ]
      },
      {
        "id": 7,
        "title": "Business Metrics - Tasks",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(tasksphere_tasks_created_total[5m])",
            "legendFormat": "Tasks Created/sec"
          },
          {
            "expr": "rate(tasksphere_tasks_completed_total[5m])",
            "legendFormat": "Tasks Completed/sec"
          }
        ]
      },
      {
        "id": 8,
        "title": "Active Users",
        "type": "stat",
        "targets": [
          {
            "expr": "tasksphere_users_active",
            "legendFormat": "Active Users"
          }
        ]
      }
    ],
    "refresh": "30s",
    "schemaVersion": 30
  }
}
```

---

## 🚨 **Alerting Setup**

### **Alertmanager Configuration**

```yaml
# alertmanager.yml
global:
  smtp_smarthost: 'localhost:587'
  smtp_from: 'alerts@tasksphere.app'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
  - name: 'web.hook'
    email_configs:
      - to: 'admin@tasksphere.app'
        subject: '🚨 TaskSphere Alert: {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          Alert: {{ .Annotations.summary }}
          Description: {{ .Annotations.description }}
          Severity: {{ .Labels.severity }}
          Instance: {{ .Labels.instance }}
          {{ end }}
    
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
        channel: '#alerts'
        title: 'TaskSphere Alert'
        text: |
          {{ range .Alerts }}
          🚨 *{{ .Annotations.summary }}*
          *Severity:* {{ .Labels.severity }}
          *Instance:* {{ .Labels.instance }}
          *Description:* {{ .Annotations.description }}
          {{ end }}
```

### **Alert Rules**

```yaml
# rules/tasksphere-alerts.yml
groups:
  - name: tasksphere-application
    rules:
      - alert: ApplicationDown
        expr: up{job="tasksphere-app"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "TaskSphere application is down"
          description: "TaskSphere application has been down for more than 1 minute"

      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors per second"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High response time"
          description: "95th percentile response time is {{ $value }}s"

      - alert: DatabaseConnectionPoolExhaustion
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "Connection pool usage is {{ $value | humanizePercentage }}"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.8
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High JVM memory usage"
          description: "JVM heap memory usage is {{ $value | humanizePercentage }}"

      - alert: LowCacheHitRate
        expr: rate(cache_gets_total{result="hit"}[5m]) / rate(cache_gets_total[5m]) < 0.7
        for: 15m
        labels:
          severity: warning
        annotations:
          summary: "Low cache hit rate"
          description: "Cache hit rate is {{ $value | humanizePercentage }}"

  - name: tasksphere-business
    rules:
      - alert: NoTasksCreated
        expr: increase(tasksphere_tasks_created_total[1h]) == 0
        for: 2h
        labels:
          severity: info
        annotations:
          summary: "No tasks created in the last hour"
          description: "No new tasks have been created in the last 2 hours"

      - alert: HighTaskCreationRate
        expr: rate(tasksphere_tasks_created_total[5m]) > 10
        for: 10m
        labels:
          severity: info
        annotations:
          summary: "High task creation rate"
          description: "Task creation rate is {{ $value }} tasks per second"
```

---

## 📝 **Structured Logging**

### **Logback Configuration**

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!prod">
        <!-- Development logging -->
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
    </springProfile>

    <springProfile name="prod">
        <!-- Production JSON logging -->
        <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeContext>true</includeContext>
                <includeMdc>true</includeMdc>
                <customFields>{"application":"tasksphere","environment":"${spring.profiles.active:-default}"}</customFields>
                <fieldNames>
                    <timestamp>@timestamp</timestamp>
                    <level>level</level>
                    <thread>thread</thread>
                    <logger>logger</logger>
                    <message>message</message>
                </fieldNames>
            </encoder>
        </appender>
    </springProfile>

    <!-- File appender for all environments -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/tasksphere-application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/tasksphere-application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>true</includeContext>
            <includeMdc>true</includeMdc>
            <customFields>{"application":"tasksphere"}</customFields>
        </encoder>
    </appender>

    <!-- Async appender for performance -->
    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="FILE"/>
        <queueSize>256</queueSize>
        <discardingThreshold>0</discardingThreshold>
    </appender>

    <!-- Application loggers -->
    <logger name="com.tasksphere" level="INFO"/>
    <logger name="com.tasksphere.security" level="DEBUG"/>
    
    <!-- Framework loggers -->
    <logger name="org.springframework.security" level="INFO"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    <logger name="com.zaxxer.hikari" level="INFO"/>

    <root level="INFO">
        <springProfile name="!prod">
            <appender-ref ref="CONSOLE"/>
        </springProfile>
        <springProfile name="prod">
            <appender-ref ref="JSON_CONSOLE"/>
        </springProfile>
        <appender-ref ref="ASYNC_FILE"/>
    </root>
</configuration>
```

### **Structured Logging in Code**

```java
@Component
public class AuditLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);
    
    public void logUserAction(String userId, String action, String resource) {
        MDC.put("userId", userId);
        MDC.put("action", action);
        MDC.put("resource", resource);
        MDC.put("timestamp", Instant.now().toString());
        
        logger.info("User action performed: {} on {}", action, resource);
        
        MDC.clear();
    }
    
    public void logApiRequest(HttpServletRequest request, long duration) {
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("duration", String.valueOf(duration));
        MDC.put("userAgent", request.getHeader("User-Agent"));
        
        logger.info("API request processed in {}ms", duration);
        
        MDC.clear();
    }
}
```

---

## ☁️ **AWS CloudWatch Integration**

### **CloudWatch Agent Configuration**

```json
{
  "agent": {
    "metrics_collection_interval": 60,
    "run_as_user": "cwagent"
  },
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/app/logs/tasksphere-application.log",
            "log_group_name": "/aws/ecs/tasksphere",
            "log_stream_name": "{instance_id}-application",
            "timezone": "UTC",
            "multi_line_start_pattern": "^\\d{4}-\\d{2}-\\d{2}"
          }
        ]
      }
    }
  },
  "metrics": {
    "namespace": "TaskSphere/Application",
    "metrics_collected": {
      "cpu": {
        "measurement": [
          "cpu_usage_idle",
          "cpu_usage_iowait",
          "cpu_usage_user",
          "cpu_usage_system"
        ],
        "metrics_collection_interval": 60
      },
      "disk": {
        "measurement": [
          "used_percent"
        ],
        "metrics_collection_interval": 60,
        "resources": [
          "*"
        ]
      },
      "diskio": {
        "measurement": [
          "io_time"
        ],
        "metrics_collection_interval": 60,
        "resources": [
          "*"
        ]
      },
      "mem": {
        "measurement": [
          "mem_used_percent"
        ],
        "metrics_collection_interval": 60
      }
    }
  }
}
```

### **Custom CloudWatch Metrics**

```java
@Component
public class CloudWatchMetrics {
    
    private final AmazonCloudWatch cloudWatch;
    
    public CloudWatchMetrics(AmazonCloudWatch cloudWatch) {
        this.cloudWatch = cloudWatch;
    }
    
    public void publishBusinessMetric(String metricName, double value, String unit) {
        MetricDatum metric = new MetricDatum()
            .withMetricName(metricName)
            .withValue(value)
            .withUnit(unit)
            .withTimestamp(new Date());
            
        PutMetricDataRequest request = new PutMetricDataRequest()
            .withNamespace("TaskSphere/Business")
            .withMetricData(metric);
            
        cloudWatch.putMetricData(request);
    }
    
    @EventListener
    public void handleTaskCreated(TaskCreatedEvent event) {
        publishBusinessMetric("TasksCreated", 1.0, "Count");
    }
    
    @EventListener
    public void handleUserLogin(UserLoginEvent event) {
        publishBusinessMetric("UserLogins", 1.0, "Count");
    }
}
```

---

## 📊 **Performance Monitoring**

### **Application Performance Monitoring (APM)**

```java
@Component
public class PerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public PerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        // Monitor JVM metrics
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        
        // Monitor system metrics
        new FileDescriptorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

// Performance monitoring annotations
@RestController
public class TaskController {
    
    @GetMapping("/tasks")
    @Timed(name = "tasks.list", description = "Time taken to list tasks")
    @Counted(name = "tasks.list.requests", description = "Number of task list requests")
    public ResponseEntity<List<Task>> listTasks() {
        // Implementation
    }
}
```

### **Database Performance Monitoring**

```java
@Component
public class DatabaseMetrics {
    
    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;
    
    public DatabaseMetrics(DataSource dataSource, MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
        
        // Register HikariCP metrics
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).setMetricRegistry(meterRegistry);
        }
    }
    
    @Scheduled(fixedRate = 30000)
    public void reportDatabaseStats() {
        try (Connection connection = dataSource.getConnection()) {
            // Query database statistics
            try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) as active_connections FROM information_schema.processlist WHERE command != 'Sleep'")) {
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int activeConnections = rs.getInt("active_connections");
                    Gauge.builder("database.connections.active")
                        .description("Active database connections")
                        .register(meterRegistry, () -> activeConnections);
                }
            }
        } catch (SQLException e) {
            // Log error
        }
    }
}
```

---

## 🔍 **Distributed Tracing**

### **Jaeger Integration**

```yaml
# application.yml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://jaeger:14268/api/traces

spring:
  application:
    name: tasksphere-backend
```

```java
@RestController
public class TracedController {
    
    private final TaskService taskService;
    private final Tracer tracer;
    
    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        Span span = tracer.nextSpan()
            .name("get-task")
            .tag("task.id", id.toString())
            .start();
            
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            Task task = taskService.findById(id);
            span.tag("task.status", task.getStatus());
            return ResponseEntity.ok(task);
        } finally {
            span.end();
        }
    }
}
```

---

## 📱 **Mobile App Monitoring**

### **Crash Reporting**

```javascript
// React Native - Crashlytics
import crashlytics from '@react-native-firebase/crashlytics';

// Log non-fatal errors
crashlytics().recordError(new Error('TaskSphere API error'));

// Custom logging
crashlytics().log('User created a new task');

// User identification
crashlytics().setUserId(user.id);
crashlytics().setAttributes({
  role: user.role,
  team: user.team
});
```

### **Performance Monitoring**

```javascript
// React Native - Performance
import perf from '@react-native-firebase/perf';

// Trace API calls
const trace = await perf().startTrace('task_creation');
trace.putAttribute('task_type', 'bug');
trace.putMetric('task_count', 1);

try {
  await createTask(taskData);
  trace.putMetric('success', 1);
} catch (error) {
  trace.putMetric('error', 1);
} finally {
  await trace.stop();
}
```

---

## 🚨 **Incident Response**

### **Runbook Template**

```markdown
# TaskSphere Incident Response Runbook

## Severity Levels
- **P0 (Critical)**: Service completely down
- **P1 (High)**: Major functionality impaired
- **P2 (Medium)**: Minor functionality affected
- **P3 (Low)**: Enhancement or optimization

## Response Procedures

### Application Down (P0)
1. Check application health: `curl https://api.tasksphere.app/actuator/health`
2. Check ECS service status
3. Check database connectivity
4. Check Redis connectivity
5. Restart service if necessary
6. Escalate to on-call engineer

### High Error Rate (P1)
1. Check application logs
2. Identify error patterns
3. Check recent deployments
4. Monitor error trends
5. Implement hotfix if needed

### Database Issues (P1)
1. Check RDS status
2. Monitor connection pool metrics
3. Check slow query logs
4. Scale RDS if needed
5. Optimize queries if necessary
```

### **On-Call Schedule**

```yaml
# PagerDuty integration
services:
  - name: TaskSphere Production
    escalation_policy: TaskSphere Escalation
    integrations:
      - prometheus
      - cloudwatch

schedules:
  - name: Primary On-Call
    users:
      - manohar.pagadala@example.com
    rotation: weekly
```

---

## 📞 **Support & Troubleshooting**

### **Common Issues**

| Issue | Symptoms | Solution |
|-------|----------|----------|
| High Memory Usage | JVM heap > 80% | Scale up instances or optimize code |
| Database Timeouts | Connection pool exhaustion | Increase pool size or optimize queries |
| Cache Misses | Low hit rate < 70% | Review cache strategy and TTL |
| API Errors | 5xx errors > 1% | Check logs and recent deployments |

### **Monitoring Contacts**

- **Primary**: manohar.pagadala@example.com
- **Escalation**: devops-team@tasksphere.app
- **Emergency**: +1-xxx-xxx-xxxx

---

**📊 Your comprehensive monitoring stack is ready for production!**