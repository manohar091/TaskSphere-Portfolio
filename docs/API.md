# 📋 TaskSphere API Documentation

## 🌐 **API Overview**

TaskSphere provides a comprehensive RESTful API for managing tasks, projects, users, and teams. The API follows REST principles and returns JSON responses.

**Base URL**: `https://api.tasksphere.app/api/v1`

**Authentication**: Bearer Token (JWT)

**Content Type**: `application/json`

---

## 🔐 **Authentication**

### **Login**

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

### **Register**

```http
POST /auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "securePassword123",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+1-555-0123"
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 2,
    "email": "newuser@example.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "USER",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### **Refresh Token**

```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### **Logout**

```http
POST /auth/logout
Authorization: Bearer <access_token>
```

---

## 📋 **Tasks API**

### **Get All Tasks**

```http
GET /tasks?page=0&size=20&sort=createdAt,desc&status=TODO&priority=HIGH
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20, max: 100)
- `sort` (optional): Sort criteria (default: `createdAt,desc`)
- `status` (optional): Filter by status (`TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)
- `priority` (optional): Filter by priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- `assignedTo` (optional): Filter by assignee ID
- `projectId` (optional): Filter by project ID
- `search` (optional): Search in title and description

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Implement user authentication",
      "description": "Add JWT-based authentication system",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "assignedTo": {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com"
      },
      "createdBy": {
        "id": 2,
        "firstName": "Jane",
        "lastName": "Smith",
        "email": "jane.smith@example.com"
      },
      "project": {
        "id": 1,
        "name": "TaskSphere Backend",
        "description": "Backend development project"
      },
      "dueDate": "2024-02-01T23:59:59Z",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-16T14:20:00Z",
      "tags": ["authentication", "security", "backend"],
      "estimatedHours": 16,
      "actualHours": 8,
      "completionPercentage": 60
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "direction": "DESC",
      "properties": ["createdAt"]
    }
  },
  "totalElements": 45,
  "totalPages": 3,
  "first": true,
  "last": false,
  "numberOfElements": 20
}
```

### **Get Task by ID**

```http
GET /tasks/{id}
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "id": 1,
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication system with role-based access control",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "assignedTo": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "avatar": "https://example.com/avatars/john.jpg"
  },
  "createdBy": {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com"
  },
  "project": {
    "id": 1,
    "name": "TaskSphere Backend",
    "description": "Backend development project",
    "status": "ACTIVE"
  },
  "dueDate": "2024-02-01T23:59:59Z",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-16T14:20:00Z",
  "completedAt": null,
  "tags": ["authentication", "security", "backend"],
  "estimatedHours": 16,
  "actualHours": 8,
  "completionPercentage": 60,
  "attachments": [
    {
      "id": 1,
      "fileName": "auth-diagram.png",
      "fileSize": 2048576,
      "contentType": "image/png",
      "uploadedAt": "2024-01-15T11:00:00Z"
    }
  ],
  "comments": [
    {
      "id": 1,
      "content": "Working on JWT implementation",
      "author": {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe"
      },
      "createdAt": "2024-01-16T09:30:00Z"
    }
  ]
}
```

### **Create Task**

```http
POST /tasks
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "title": "Design database schema",
  "description": "Create comprehensive database schema for the application",
  "priority": "HIGH",
  "assignedToId": 1,
  "projectId": 1,
  "dueDate": "2024-02-15T23:59:59Z",
  "tags": ["database", "design", "backend"],
  "estimatedHours": 12
}
```

**Response:**
```json
{
  "id": 15,
  "title": "Design database schema",
  "description": "Create comprehensive database schema for the application",
  "status": "TODO",
  "priority": "HIGH",
  "assignedTo": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  },
  "createdBy": {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com"
  },
  "project": {
    "id": 1,
    "name": "TaskSphere Backend"
  },
  "dueDate": "2024-02-15T23:59:59Z",
  "createdAt": "2024-01-16T15:45:00Z",
  "updatedAt": "2024-01-16T15:45:00Z",
  "tags": ["database", "design", "backend"],
  "estimatedHours": 12,
  "actualHours": 0,
  "completionPercentage": 0
}
```

### **Update Task**

```http
PUT /tasks/{id}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "title": "Design and implement database schema",
  "description": "Create comprehensive database schema and implement with migrations",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "assignedToId": 1,
  "dueDate": "2024-02-20T23:59:59Z",
  "tags": ["database", "design", "implementation", "backend"],
  "estimatedHours": 16,
  "actualHours": 4,
  "completionPercentage": 25
}
```

### **Update Task Status**

```http
PATCH /tasks/{id}/status
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "status": "COMPLETED",
  "completionNotes": "Task completed successfully with full test coverage"
}
```

### **Assign Task**

```http
PATCH /tasks/{id}/assign
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "assignedToId": 3,
  "assignmentNotes": "Reassigning to John due to expertise in this area"
}
```

### **Delete Task**

```http
DELETE /tasks/{id}
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "message": "Task deleted successfully",
  "deletedAt": "2024-01-16T16:30:00Z"
}
```

---

## 📁 **Projects API**

### **Get All Projects**

```http
GET /projects?page=0&size=20&status=ACTIVE
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "TaskSphere Backend",
      "description": "Backend development for TaskSphere application",
      "status": "ACTIVE",
      "priority": "HIGH",
      "owner": {
        "id": 2,
        "firstName": "Jane",
        "lastName": "Smith",
        "email": "jane.smith@example.com"
      },
      "team": {
        "id": 1,
        "name": "Development Team",
        "memberCount": 5
      },
      "startDate": "2024-01-01T00:00:00Z",
      "endDate": "2024-06-30T23:59:59Z",
      "createdAt": "2024-01-01T09:00:00Z",
      "updatedAt": "2024-01-16T10:00:00Z",
      "taskCount": 45,
      "completedTaskCount": 23,
      "progressPercentage": 51,
      "tags": ["backend", "spring-boot", "api"]
    }
  ],
  "totalElements": 12,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### **Get Project by ID**

```http
GET /projects/{id}
Authorization: Bearer <access_token>
```

### **Create Project**

```http
POST /projects
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Mobile Application",
  "description": "React Native mobile app for TaskSphere",
  "priority": "MEDIUM",
  "teamId": 1,
  "startDate": "2024-02-01T00:00:00Z",
  "endDate": "2024-08-31T23:59:59Z",
  "tags": ["mobile", "react-native", "frontend"]
}
```

### **Update Project**

```http
PUT /projects/{id}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "TaskSphere Mobile App",
  "description": "Cross-platform mobile application for TaskSphere",
  "status": "ACTIVE",
  "priority": "HIGH",
  "endDate": "2024-09-30T23:59:59Z",
  "tags": ["mobile", "react-native", "ios", "android"]
}
```

---

## 👥 **Users API**

### **Get Current User Profile**

```http
GET /users/me
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER",
  "phoneNumber": "+1-555-0123",
  "avatar": "https://example.com/avatars/john.jpg",
  "timezone": "America/New_York",
  "language": "en",
  "isActive": true,
  "emailVerified": true,
  "twoFactorEnabled": false,
  "lastLogin": "2024-01-16T08:30:00Z",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-15T14:20:00Z",
  "preferences": {
    "emailNotifications": true,
    "pushNotifications": true,
    "weeklyDigest": true,
    "theme": "light"
  },
  "statistics": {
    "tasksCreated": 23,
    "tasksCompleted": 18,
    "projectsOwned": 2,
    "projectsContributed": 5
  }
}
```

### **Update User Profile**

```http
PUT /users/me
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "firstName": "Jonathan",
  "lastName": "Doe",
  "phoneNumber": "+1-555-0199",
  "timezone": "America/Los_Angeles",
  "language": "en",
  "preferences": {
    "emailNotifications": true,
    "pushNotifications": false,
    "weeklyDigest": true,
    "theme": "dark"
  }
}
```

### **Get All Users** (Admin only)

```http
GET /users?page=0&size=20&role=USER&search=john
Authorization: Bearer <access_token>
```

### **Get User by ID**

```http
GET /users/{id}
Authorization: Bearer <access_token>
```

---

## 🔔 **Notifications API**

### **Get User Notifications**

```http
GET /notifications?page=0&size=20&unreadOnly=true
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "type": "TASK_ASSIGNED",
      "title": "New Task Assigned",
      "message": "You have been assigned to task: 'Implement user authentication'",
      "isRead": false,
      "priority": "MEDIUM",
      "createdAt": "2024-01-16T10:30:00Z",
      "data": {
        "taskId": 1,
        "taskTitle": "Implement user authentication",
        "assignedBy": "Jane Smith"
      }
    },
    {
      "id": 2,
      "type": "TASK_DUE_SOON",
      "title": "Task Due Soon",
      "message": "Task 'Design database schema' is due in 2 days",
      "isRead": false,
      "priority": "HIGH",
      "createdAt": "2024-01-16T09:00:00Z",
      "data": {
        "taskId": 15,
        "taskTitle": "Design database schema",
        "dueDate": "2024-01-18T23:59:59Z"
      }
    }
  ],
  "totalElements": 8,
  "unreadCount": 5
}
```

### **Mark Notification as Read**

```http
PATCH /notifications/{id}/read
Authorization: Bearer <access_token>
```

### **Mark All Notifications as Read**

```http
PATCH /notifications/read-all
Authorization: Bearer <access_token>
```

---

## 💬 **Comments API**

### **Get Task Comments**

```http
GET /tasks/{taskId}/comments?page=0&size=10
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "content": "Started working on this task. Setting up the basic JWT infrastructure.",
      "author": {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "avatar": "https://example.com/avatars/john.jpg"
      },
      "createdAt": "2024-01-16T09:30:00Z",
      "updatedAt": "2024-01-16T09:30:00Z",
      "isEdited": false
    },
    {
      "id": 2,
      "content": "Great progress! Let me know if you need any help with the security configuration.",
      "author": {
        "id": 2,
        "firstName": "Jane",
        "lastName": "Smith",
        "avatar": "https://example.com/avatars/jane.jpg"
      },
      "createdAt": "2024-01-16T11:15:00Z",
      "updatedAt": "2024-01-16T11:15:00Z",
      "isEdited": false
    }
  ],
  "totalElements": 5
}
```

### **Add Comment**

```http
POST /tasks/{taskId}/comments
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "content": "Completed the JWT token generation. Moving on to the authentication filter."
}
```

### **Update Comment**

```http
PUT /comments/{id}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "content": "Completed the JWT token generation and validation. Moving on to the authentication filter."
}
```

### **Delete Comment**

```http
DELETE /comments/{id}
Authorization: Bearer <access_token>
```

---

## 📊 **Analytics API**

### **Get Dashboard Statistics**

```http
GET /analytics/dashboard
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "overview": {
    "totalTasks": 156,
    "completedTasks": 89,
    "inProgressTasks": 34,
    "overdueTasks": 12,
    "totalProjects": 8,
    "activeProjects": 6,
    "teamMembers": 15
  },
  "tasksByStatus": {
    "TODO": 21,
    "IN_PROGRESS": 34,
    "COMPLETED": 89,
    "CANCELLED": 12
  },
  "tasksByPriority": {
    "LOW": 45,
    "MEDIUM": 67,
    "HIGH": 32,
    "CRITICAL": 12
  },
  "productivityMetrics": {
    "tasksCompletedThisWeek": 23,
    "averageCompletionTime": 4.5,
    "onTimeCompletion": 87.3,
    "teamEfficiency": 92.1
  },
  "recentActivity": [
    {
      "type": "TASK_COMPLETED",
      "message": "John Doe completed 'Implement user authentication'",
      "timestamp": "2024-01-16T14:30:00Z"
    },
    {
      "type": "PROJECT_CREATED",
      "message": "Jane Smith created project 'Mobile Application'",
      "timestamp": "2024-01-16T13:15:00Z"
    }
  ]
}
```

### **Get User Performance**

```http
GET /analytics/users/{userId}/performance?period=30d
Authorization: Bearer <access_token>
```

### **Get Project Progress**

```http
GET /analytics/projects/{projectId}/progress
Authorization: Bearer <access_token>
```

---

## 📁 **File Upload API**

### **Upload Task Attachment**

```http
POST /tasks/{taskId}/attachments
Authorization: Bearer <access_token>
Content-Type: multipart/form-data

Form Data:
- file: [binary file data]
- description: "Authentication flow diagram"
```

**Response:**
```json
{
  "id": 1,
  "fileName": "auth-flow-diagram.png",
  "originalFileName": "authentication-flow.png",
  "fileSize": 2048576,
  "contentType": "image/png",
  "description": "Authentication flow diagram",
  "downloadUrl": "https://api.tasksphere.app/files/download/auth-flow-diagram.png",
  "uploadedBy": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe"
  },
  "uploadedAt": "2024-01-16T15:30:00Z"
}
```

### **Download Attachment**

```http
GET /files/download/{fileName}
Authorization: Bearer <access_token>
```

### **Delete Attachment**

```http
DELETE /attachments/{id}
Authorization: Bearer <access_token>
```

---

## 🔍 **Search API**

### **Global Search**

```http
GET /search?q=authentication&type=tasks&page=0&size=20
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `q`: Search query
- `type`: Search type (`tasks`, `projects`, `users`, `all`)
- `page`: Page number
- `size`: Page size

**Response:**
```json
{
  "query": "authentication",
  "results": {
    "tasks": [
      {
        "id": 1,
        "title": "Implement user authentication",
        "description": "Add JWT-based authentication system",
        "status": "IN_PROGRESS",
        "priority": "HIGH",
        "relevanceScore": 0.95
      }
    ],
    "projects": [
      {
        "id": 1,
        "name": "TaskSphere Backend",
        "description": "Backend development with authentication",
        "status": "ACTIVE",
        "relevanceScore": 0.78
      }
    ]
  },
  "totalResults": 15,
  "searchTime": 0.045
}
```

---

## ⚠️ **Error Responses**

### **Standard Error Format**

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request data",
    "details": [
      {
        "field": "title",
        "message": "Title is required and cannot be empty"
      },
      {
        "field": "priority",
        "message": "Priority must be one of: LOW, MEDIUM, HIGH, CRITICAL"
      }
    ],
    "timestamp": "2024-01-16T10:30:00Z",
    "path": "/api/v1/tasks",
    "requestId": "req-123456789"
  }
}
```

### **Common Error Codes**

| Status Code | Error Code | Description |
|-------------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Request validation failed |
| 401 | `UNAUTHORIZED` | Authentication required |
| 403 | `FORBIDDEN` | Insufficient permissions |
| 404 | `NOT_FOUND` | Resource not found |
| 409 | `CONFLICT` | Resource already exists |
| 429 | `RATE_LIMIT_EXCEEDED` | Too many requests |
| 500 | `INTERNAL_ERROR` | Server internal error |
| 503 | `SERVICE_UNAVAILABLE` | Service temporarily unavailable |

---

## 🔧 **Rate Limiting**

### **Rate Limits by Endpoint**

| Endpoint | Rate Limit | Window |
|----------|------------|--------|
| `/auth/login` | 5 requests | 15 minutes |
| `/auth/register` | 3 requests | 1 hour |
| `/tasks/*` | 100 requests | 1 minute |
| `/projects/*` | 50 requests | 1 minute |
| `/users/*` | 30 requests | 1 minute |
| Default | 60 requests | 1 minute |

### **Rate Limit Headers**

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642343400
```

---

## 📱 **Webhooks**

### **Configure Webhook**

```http
POST /webhooks
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "url": "https://your-app.com/webhooks/tasksphere",
  "events": ["task.created", "task.updated", "task.completed"],
  "secret": "your-webhook-secret",
  "active": true
}
```

### **Webhook Events**

| Event | Description |
|-------|-------------|
| `task.created` | New task created |
| `task.updated` | Task updated |
| `task.completed` | Task marked as completed |
| `task.assigned` | Task assigned to user |
| `project.created` | New project created |
| `project.updated` | Project updated |
| `user.registered` | New user registered |

### **Webhook Payload Example**

```json
{
  "event": "task.created",
  "timestamp": "2024-01-16T10:30:00Z",
  "data": {
    "task": {
      "id": 1,
      "title": "Implement user authentication",
      "status": "TODO",
      "priority": "HIGH",
      "assignedTo": {
        "id": 1,
        "email": "john.doe@example.com"
      },
      "createdBy": {
        "id": 2,
        "email": "jane.smith@example.com"
      }
    }
  },
  "signature": "sha256=a3b5c1d2e3f4..."
}
```

---

## 🧪 **Testing the API**

### **Postman Collection**

Download our Postman collection: [TaskSphere API Collection](https://github.com/manohar-pagadala/TaskSphere-Portfolio/blob/main/docs/TaskSphere-API.postman_collection.json)

### **curl Examples**

```bash
# Login
curl -X POST https://api.tasksphere.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Get tasks
curl -X GET https://api.tasksphere.app/api/v1/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Create task
curl -X POST https://api.tasksphere.app/api/v1/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Task",
    "description": "Task description",
    "priority": "HIGH",
    "assignedToId": 1,
    "projectId": 1
  }'
```

---

## 📞 **API Support**

### **Rate Limits & Quotas**

- **Free Tier**: 1,000 requests/hour
- **Pro Tier**: 10,000 requests/hour
- **Enterprise**: Custom limits

### **Support Contacts**

- **API Documentation**: https://docs.tasksphere.app
- **Developer Support**: api-support@tasksphere.app
- **Status Page**: https://status.tasksphere.app

---

**📋 Your comprehensive API is ready for integration!**