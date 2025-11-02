# 🤝 Contributing to TaskSphere

Thank you for your interest in contributing to TaskSphere! This guide will help you get started with contributing to our enterprise task management platform.

---

## 📋 **Table of Contents**

- [Code of Conduct](#-code-of-conduct)
- [Getting Started](#-getting-started)
- [Development Setup](#️-development-setup)
- [Contributing Guidelines](#-contributing-guidelines)
- [Pull Request Process](#-pull-request-process)
- [Issue Reporting](#-issue-reporting)
- [Coding Standards](#️-coding-standards)
- [Testing Guidelines](#-testing-guidelines)
- [Documentation](#-documentation)
- [Community](#-community)

---

## 🤝 **Code of Conduct**

### **Our Pledge**

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of age, body size, disability, ethnicity, gender identity and expression, level of experience, nationality, personal appearance, race, religion, or sexual identity and orientation.

### **Our Standards**

**Examples of behavior that contributes to creating a positive environment include:**

- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

**Examples of unacceptable behavior include:**

- The use of sexualized language or imagery and unwelcome sexual attention or advances
- Trolling, insulting/derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information without explicit permission
- Other conduct which could reasonably be considered inappropriate in a professional setting

### **Enforcement**

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting the project team at conduct@tasksphere.app. All complaints will be reviewed and investigated promptly and fairly.

---

## 🚀 **Getting Started**

### **Ways to Contribute**

- 🐛 **Bug Reports**: Help us identify and fix issues
- 💡 **Feature Requests**: Suggest new features or improvements
- 📝 **Documentation**: Improve our documentation
- 🧪 **Testing**: Help us improve test coverage
- 💻 **Code**: Contribute code improvements and new features
- 🎨 **UI/UX**: Improve user interface and experience
- 🌐 **Translations**: Help make TaskSphere available in more languages

### **Before You Start**

1. **Check existing issues** to see if your bug report or feature request already exists
2. **Read this contributing guide** thoroughly
3. **Set up your development environment** following our setup guide
4. **Familiarize yourself** with our codebase and architecture

---

## 🛠️ **Development Setup**

### **Prerequisites**

Ensure you have the following installed:

```bash
# Java Development Kit 21
java -version
# openjdk version "21.0.1" 2023-10-17

# Maven 3.9+
mvn -version
# Apache Maven 3.9.6

# Docker & Docker Compose
docker --version
docker-compose --version

# Git
git --version

# Node.js 18+ (for frontend development)
node --version
npm --version
```

### **Fork and Clone**

```bash
# 1. Fork the repository on GitHub
# 2. Clone your fork
git clone https://github.com/YOUR_USERNAME/TaskSphere-Portfolio.git
cd TaskSphere-Portfolio

# 3. Add upstream remote
git remote add upstream https://github.com/manohar-pagadala/TaskSphere-Portfolio.git

# 4. Verify remotes
git remote -v
```

### **Local Development Setup**

```bash
# 1. Start infrastructure services
cd infra
docker-compose up -d

# 2. Wait for services to be ready
# MySQL: localhost:3306
# Redis: localhost:6379
# Prometheus: localhost:9090
# Grafana: localhost:3000

# 3. Setup database
cd ../backend
mvn flyway:migrate

# 4. Build and run application
mvn clean install
mvn spring-boot:run

# 5. Verify application is running
curl http://localhost:8081/actuator/health
```

### **Frontend Setup (if contributing to UI)**

```bash
cd frontend
npm install
npm start

# Frontend will be available at http://localhost:3000
```

### **Development Tools Setup**

```bash
# Install recommended VS Code extensions
code --install-extension vscjava.vscode-java-pack
code --install-extension SonarSource.sonarlint-vscode
code --install-extension ms-vscode.vscode-typescript-next
code --install-extension esbenp.prettier-vscode

# Configure Git hooks (optional but recommended)
cd .git/hooks
cp ../scripts/pre-commit.sh pre-commit
chmod +x pre-commit
```

---

## 📝 **Contributing Guidelines**

### **Branch Naming Convention**

Use descriptive branch names with the following prefixes:

```bash
# Features
feature/task-priority-sorting
feature/user-profile-enhancement

# Bug fixes
bugfix/login-validation-error
bugfix/memory-leak-in-cache

# Documentation
docs/contributing-guide-update
docs/api-documentation-improvement

# Hotfixes
hotfix/security-vulnerability-fix
hotfix/database-connection-issue

# Refactoring
refactor/service-layer-cleanup
refactor/test-structure-improvement
```

### **Commit Message Format**

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```bash
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types:**

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding missing tests or correcting existing tests
- `chore`: Changes to the build process or auxiliary tools

**Examples:**

```bash
feat(auth): add OAuth2 integration for Google login

fix(database): resolve connection pool exhaustion issue

docs(readme): update installation instructions

test(user-service): add unit tests for user registration

chore(deps): update Spring Boot to 3.5.7
```

### **Code Review Process**

1. **Self-review** your changes before submitting
2. **Run all tests** and ensure they pass
3. **Check code coverage** meets minimum requirements (80%)
4. **Update documentation** if necessary
5. **Submit pull request** with clear description
6. **Address review feedback** promptly and professionally
7. **Squash commits** before merge if requested

---

## 🔄 **Pull Request Process**

### **Before Submitting**

```bash
# 1. Sync with upstream
git fetch upstream
git checkout main
git merge upstream/main

# 2. Create feature branch
git checkout -b feature/your-feature-name

# 3. Make your changes
# ... code changes ...

# 4. Run tests
mvn clean test
npm test # if frontend changes

# 5. Run code quality checks
mvn spotbugs:check
mvn checkstyle:check
npm run lint # if frontend changes

# 6. Update documentation
# Update relevant .md files
# Update inline code documentation

# 7. Commit changes
git add .
git commit -m "feat(scope): description of changes"

# 8. Push to your fork
git push origin feature/your-feature-name
```

### **Pull Request Template**

When creating a pull request, use this template:

```markdown
## 📋 Description

Brief description of what this PR does.

## 🔗 Related Issue

Fixes #(issue_number)

## 🛠️ Type of Change

- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Code refactoring

## 📝 How Has This Been Tested?

- [ ] Unit tests
- [ ] Integration tests
- [ ] Manual testing
- [ ] Load testing (if applicable)

Describe the tests that you ran to verify your changes.

## ✅ Checklist

- [ ] My code follows the project's coding standards
- [ ] I have performed a self-review of my own code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
- [ ] Any dependent changes have been merged and published

## 📸 Screenshots (if applicable)

Add screenshots to help explain your changes.

## 🔗 Additional Notes

Any additional information that reviewers should know.
```

### **Review Criteria**

Your pull request will be reviewed based on:

- **Code Quality**: Clean, readable, and maintainable code
- **Testing**: Adequate test coverage and passing tests
- **Documentation**: Updated documentation where necessary
- **Performance**: No significant performance regressions
- **Security**: No introduction of security vulnerabilities
- **Architecture**: Consistency with project architecture
- **Standards**: Adherence to coding standards and conventions

---

## 🐛 **Issue Reporting**

### **Bug Reports**

Use the bug report template:

```markdown
**Bug Description**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected Behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment**
- OS: [e.g. Windows 10, macOS 12.6, Ubuntu 20.04]
- Browser: [e.g. Chrome 98, Safari 15.4]
- Java Version: [e.g. 21.0.1]
- Application Version: [e.g. 1.2.3]

**Additional Context**
Add any other context about the problem here.

**Logs**
If applicable, include relevant log outputs.
```

### **Feature Requests**

Use the feature request template:

```markdown
**Is your feature request related to a problem? Please describe.**
A clear and concise description of what the problem is. Ex. I'm always frustrated when [...]

**Describe the solution you'd like**
A clear and concise description of what you want to happen.

**Describe alternatives you've considered**
A clear and concise description of any alternative solutions or features you've considered.

**Additional context**
Add any other context or screenshots about the feature request here.

**Acceptance Criteria**
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3
```

---

## 🎯 **Coding Standards**

### **Java Coding Standards**

#### **Code Formatting**

```java
// Use 4 spaces for indentation (no tabs)
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    
    public TaskService(TaskRepository taskRepository, 
                      NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }
    
    public Task createTask(CreateTaskRequest request) {
        // Validate input
        validateTaskRequest(request);
        
        // Create task entity
        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(request.getPriority())
            .status(TaskStatus.TODO)
            .createdAt(LocalDateTime.now())
            .build();
        
        // Save and return
        Task savedTask = taskRepository.save(task);
        
        // Send notification
        notificationService.sendTaskCreatedNotification(savedTask);
        
        return savedTask;
    }
}
```

#### **Naming Conventions**

```java
// Classes: PascalCase
public class TaskManagementService {}

// Methods and variables: camelCase
public void updateTaskStatus() {}
private String userName;

// Constants: UPPER_SNAKE_CASE
private static final String DEFAULT_PRIORITY = "MEDIUM";
private static final int MAX_RETRY_ATTEMPTS = 3;

// Packages: lowercase with dots
package com.tasksphere.service.impl;

// Enums: PascalCase with UPPER_CASE values
public enum TaskStatus {
    TODO, IN_PROGRESS, COMPLETED, CANCELLED
}
```

#### **Documentation**

```java
/**
 * Service responsible for managing task operations including creation,
 * modification, assignment, and status tracking.
 * 
 * <p>This service handles all business logic related to tasks and ensures
 * proper validation, notification, and audit logging.
 * 
 * @author Manohar Pagadala
 * @since 1.0.0
 */
@Service
@Transactional
public class TaskService {
    
    /**
     * Creates a new task with the provided details.
     * 
     * <p>This method validates the input, creates the task entity,
     * saves it to the database, and sends appropriate notifications.
     * 
     * @param request the task creation request containing task details
     * @return the created task with generated ID and metadata
     * @throws ValidationException if the request contains invalid data
     * @throws SecurityException if the user lacks permission to create tasks
     * @since 1.0.0
     */
    public Task createTask(CreateTaskRequest request) {
        // Implementation
    }
}
```

### **JavaScript/TypeScript Standards**

```typescript
// Use 2 spaces for indentation
// Use semicolons
// Use single quotes for strings
// Use const/let instead of var

interface TaskRequest {
  title: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  assigneeId?: number;
  dueDate?: Date;
}

class TaskService {
  private readonly apiClient: ApiClient;

  constructor(apiClient: ApiClient) {
    this.apiClient = apiClient;
  }

  public async createTask(request: TaskRequest): Promise<Task> {
    // Validate input
    this.validateTaskRequest(request);

    try {
      const response = await this.apiClient.post('/tasks', request);
      return response.data;
    } catch (error) {
      throw new TaskCreationError('Failed to create task', error);
    }
  }

  private validateTaskRequest(request: TaskRequest): void {
    if (!request.title || request.title.trim().length === 0) {
      throw new ValidationError('Task title is required');
    }

    if (request.title.length > 200) {
      throw new ValidationError('Task title cannot exceed 200 characters');
    }
  }
}
```

---

## 🧪 **Testing Guidelines**

### **Test Structure**

```java
// Follow AAA pattern: Arrange, Act, Assert
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private TaskService taskService;
    
    @Test
    @DisplayName("Should create task successfully with valid request")
    void shouldCreateTaskSuccessfully() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .priority(Priority.MEDIUM)
            .build();
        
        Task expectedTask = Task.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .priority(Priority.MEDIUM)
            .status(TaskStatus.TODO)
            .build();
        
        when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);
        
        // Act
        Task actualTask = taskService.createTask(request);
        
        // Assert
        assertThat(actualTask).isNotNull();
        assertThat(actualTask.getId()).isEqualTo(1L);
        assertThat(actualTask.getTitle()).isEqualTo("Test Task");
        assertThat(actualTask.getStatus()).isEqualTo(TaskStatus.TODO);
        
        verify(taskRepository).save(any(Task.class));
        verify(notificationService).sendTaskCreatedNotification(expectedTask);
    }
    
    @Test
    @DisplayName("Should throw ValidationException when title is empty")
    void shouldThrowValidationExceptionWhenTitleIsEmpty() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("")
            .description("Test Description")
            .build();
        
        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Task title is required");
        
        verify(taskRepository, never()).save(any(Task.class));
        verify(notificationService, never()).sendTaskCreatedNotification(any(Task.class));
    }
}
```

### **Integration Tests**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TaskControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Test
    @DisplayName("Should create task via REST API")
    void shouldCreateTaskViaRestApi() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Integration Test Task")
            .description("Created via REST API")
            .priority(Priority.HIGH)
            .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getValidJwtToken());
        
        HttpEntity<CreateTaskRequest> entity = new HttpEntity<>(request, headers);
        
        // Act
        ResponseEntity<TaskResponse> response = restTemplate.postForEntity(
            "/api/v1/tasks", entity, TaskResponse.class);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Integration Test Task");
        
        // Verify database state
        Optional<Task> savedTask = taskRepository.findById(response.getBody().getId());
        assertThat(savedTask).isPresent();
        assertThat(savedTask.get().getTitle()).isEqualTo("Integration Test Task");
    }
}
```

### **Test Coverage Requirements**

- **Minimum overall coverage**: 80%
- **Service layer coverage**: 90%
- **Controller layer coverage**: 85%
- **Repository layer coverage**: 70% (mostly integration tests)

```bash
# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## 📚 **Documentation**

### **Code Documentation**

- **All public methods** must have Javadoc comments
- **Complex algorithms** should be explained with inline comments
- **API endpoints** must be documented with OpenAPI annotations
- **Configuration properties** should be documented

### **README Updates**

When adding new features, update relevant sections:

- Installation instructions
- Configuration options
- API examples
- Troubleshooting guides

### **API Documentation**

```java
@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task management operations")
public class TaskController {
    
    @PostMapping
    @Operation(
        summary = "Create a new task",
        description = "Creates a new task with the provided details. Requires authentication.",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Task created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Authentication required"
            )
        }
    )
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        // Implementation
    }
}
```

---

## 🌟 **Community**

### **Communication Channels**

- **GitHub Discussions**: For general questions and discussions
- **GitHub Issues**: For bug reports and feature requests
- **Email**: manohar.pagadala@example.com for direct contact
- **Discord** (if available): Real-time chat with other contributors

### **Getting Help**

1. **Check the documentation** first
2. **Search existing issues** for similar problems
3. **Ask in GitHub Discussions** for general questions
4. **Create a detailed issue** for bugs or feature requests

### **Recognition**

We value all contributions! Contributors will be:

- Listed in our `CONTRIBUTORS.md` file
- Mentioned in release notes for significant contributions
- Invited to join our contributor Discord channel
- Considered for maintainer role based on consistent contributions

### **Contributor Levels**

- **First-time Contributors**: Welcome! We're here to help you get started
- **Regular Contributors**: Trusted community members with commit access
- **Core Maintainers**: Project stewards with full access and responsibilities

---

## 📞 **Contact & Support**

### **Maintainers**

- **Manohar Pagadala** (Project Lead)
  - GitHub: [@manohar-pagadala](https://github.com/manohar-pagadala)
  - Email: manohar.pagadala@example.com

### **Community Resources**

- **Project Website**: https://tasksphere.app
- **Documentation**: https://docs.tasksphere.app
- **Blog**: https://blog.tasksphere.app
- **Status Page**: https://status.tasksphere.app

---

## 📄 **License**

By contributing to TaskSphere, you agree that your contributions will be licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

**Thank you for contributing to TaskSphere! 🚀**

Together, we're building an amazing task management platform that helps teams around the world stay organized and productive.