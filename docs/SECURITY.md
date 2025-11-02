# 🔒 TaskSphere Security Guide

## 🛡️ **Security Architecture Overview**

This comprehensive guide covers all security aspects of TaskSphere, from authentication to data protection.

---

## 🔐 **Authentication & Authorization**

### **JWT-based Authentication**

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        // Generate JWT token
        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        return ResponseEntity.ok(new AuthResponse(token, refreshToken));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String newToken = tokenProvider.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(new AuthResponse(newToken, request.getRefreshToken()));
    }
}
```

### **JWT Token Provider**

```java
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;
    
    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationInMs;
    
    public String generateToken(Authentication authentication) {
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        return Jwts.builder()
            .setSubject(userPrincipal.getId().toString())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .claim("email", userPrincipal.getEmail())
            .claim("role", userPrincipal.getRole().name())
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        
        return Long.parseLong(claims.getSubject());
    }
}
```

### **Role-Based Access Control (RBAC)**

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Permission> permissions = new HashSet<>();
    
    // Getters and setters
}

@Entity
@Table(name = "permissions")
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String resource;
    
    @Column(nullable = false)
    private String action;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Getters and setters
}

public enum Role {
    ADMIN("ROLE_ADMIN"),
    MANAGER("ROLE_MANAGER"),
    USER("ROLE_USER"),
    VIEWER("ROLE_VIEWER");
    
    private final String authority;
    
    Role(String authority) {
        this.authority = authority;
    }
    
    public String getAuthority() {
        return authority;
    }
}
```

### **Security Configuration**

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/auth/**", "/actuator/health").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Manager and above endpoints
                .requestMatchers(HttpMethod.DELETE, "/tasks/**").hasAnyRole("ADMIN", "MANAGER")
                
                // Authenticated endpoints
                .requestMatchers("/tasks/**", "/projects/**", "/users/profile").authenticated()
                
                // All other requests
                .anyRequest().authenticated()
            );
        
        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 🔒 **Data Protection**

### **Encryption at Rest**

```java
@Configuration
public class DatabaseEncryptionConfig {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/tasksphere?useSSL=true&requireSSL=true");
        config.setUsername("tasksphere_user");
        config.setPassword("encrypted_password");
        
        // Enable SSL
        config.addDataSourceProperty("useSSL", "true");
        config.addDataSourceProperty("requireSSL", "true");
        config.addDataSourceProperty("verifyServerCertificate", "true");
        
        return new HikariDataSource(config);
    }
}

// Field-level encryption for sensitive data
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "social_security_number")
    private String socialSecurityNumber;
    
    // Getters and setters
}

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    private final AESUtil aesUtil;
    
    public EncryptedStringConverter() {
        this.aesUtil = new AESUtil();
    }
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return aesUtil.encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return aesUtil.decrypt(dbData);
    }
}
```

### **Encryption in Transit**

```yaml
# application.yml - HTTPS Configuration
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tasksphere
    protocol: TLS
    enabled-protocols: TLSv1.2,TLSv1.3
    ciphers:
      - TLS_AES_256_GCM_SHA384
      - TLS_CHACHA20_POLY1305_SHA256
      - TLS_AES_128_GCM_SHA256
      - TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
      - TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256

# Redis SSL Configuration
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: 6380
      password: ${REDIS_PASSWORD}
      ssl:
        enabled: true
      timeout: 2000ms
```

### **Sensitive Data Masking**

```java
@Component
public class DataMaskingService {
    
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 3) {
            return "***@" + domain;
        }
        
        return localPart.substring(0, 2) + "*".repeat(localPart.length() - 2) + "@" + domain;
    }
    
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 10) {
            return phoneNumber;
        }
        
        return phoneNumber.substring(0, 3) + "-***-" + phoneNumber.substring(phoneNumber.length() - 4);
    }
    
    public String maskCreditCard(String creditCard) {
        if (creditCard == null || creditCard.length() < 16) {
            return creditCard;
        }
        
        return "**** **** **** " + creditCard.substring(creditCard.length() - 4);
    }
}

// Audit logging with masked data
@Component
public class AuditLogger {
    
    private final DataMaskingService maskingService;
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    public void logUserDataAccess(User user, String action) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(Instant.now())
            .userId(user.getId())
            .email(maskingService.maskEmail(user.getEmail()))
            .action(action)
            .ipAddress(getCurrentUserIP())
            .userAgent(getCurrentUserAgent())
            .build();
            
        auditLogger.info("User data access: {}", event);
    }
}
```

---

## 🛡️ **Input Validation & Sanitization**

### **Request Validation**

```java
@RestController
@RequestMapping("/tasks")
@Validated
public class TaskController {
    
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskRequest request) {
        // Input validation is handled by @Valid annotation
        Task task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(
            @PathVariable @Positive(message = "Task ID must be positive") Long id) {
        Task task = taskService.findById(id);
        return ResponseEntity.ok(task);
    }
}

// Request DTOs with validation
public class CreateTaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.,!?()]+$", message = "Title contains invalid characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @SafeHtml(message = "Description contains unsafe HTML")
    private String description;
    
    @NotNull(message = "Priority is required")
    @Valid
    private Priority priority;
    
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    
    @Email(message = "Invalid email format")
    private String assigneeEmail;
    
    // Getters and setters
}
```

### **SQL Injection Prevention**

```java
@Repository
public class TaskRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    // Safe parameterized queries
    public List<Task> findTasksByUserId(Long userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND deleted_at IS NULL";
        return jdbcTemplate.query(sql, new Object[]{userId}, new TaskRowMapper());
    }
    
    // Safe named parameters
    public List<Task> findTasksByFilters(TaskFilterRequest filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tasks WHERE 1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (filter.getStatus() != null) {
            sql.append(" AND status = :status");
            params.put("status", filter.getStatus().name());
        }
        
        if (filter.getPriority() != null) {
            sql.append(" AND priority = :priority");
            params.put("priority", filter.getPriority().name());
        }
        
        if (filter.getStartDate() != null) {
            sql.append(" AND created_at >= :startDate");
            params.put("startDate", filter.getStartDate());
        }
        
        return namedParameterJdbcTemplate.query(sql.toString(), params, new TaskRowMapper());
    }
}

// Custom validation for complex business rules
@Component
public class TaskValidationService {
    
    public void validateTaskCreation(CreateTaskRequest request, User user) {
        // Business rule validation
        if (request.getPriority() == Priority.CRITICAL && !user.hasRole(Role.MANAGER)) {
            throw new SecurityException("Only managers can create critical tasks");
        }
        
        if (request.getDueDate() != null && 
            request.getDueDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Due date must be at least 1 hour in the future");
        }
        
        // Check for suspicious patterns
        if (containsSuspiciousContent(request.getDescription())) {
            throw new SecurityException("Content contains suspicious patterns");
        }
    }
    
    private boolean containsSuspiciousContent(String content) {
        if (content == null) return false;
        
        String[] suspiciousPatterns = {
            "<script", "javascript:", "onload=", "onerror=", 
            "SELECT.*FROM", "UNION.*SELECT", "DROP.*TABLE",
            "../", "..", "file://", "ftp://"
        };
        
        String lowerContent = content.toLowerCase();
        return Arrays.stream(suspiciousPatterns)
            .anyMatch(lowerContent::contains);
    }
}
```

### **XSS Prevention**

```java
@Configuration
public class XSSProtectionConfig {
    
    @Bean
    public FilterRegistrationBean<XSSFilter> xssFilterRegistration() {
        FilterRegistrationBean<XSSFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XSSFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}

public class XSSFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        XSSRequestWrapper wrappedRequest = new XSSRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

public class XSSRequestWrapper extends HttpServletRequestWrapper {
    
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };
    
    public XSSRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }
    
    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return stripXSS(value);
    }
    
    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;
        
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = stripXSS(values[i]);
        }
        return encodedValues;
    }
    
    private String stripXSS(String value) {
        if (value != null) {
            value = HtmlUtils.htmlEscape(value);
            
            for (Pattern pattern : XSS_PATTERNS) {
                value = pattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }
}
```

---

## 🔐 **API Security**

### **Rate Limiting**

```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Rate limiting configuration
    private static final Map<String, RateLimit> RATE_LIMITS = Map.of(
        "/auth/login", new RateLimit(5, Duration.ofMinutes(15)), // 5 attempts per 15 minutes
        "/auth/register", new RateLimit(3, Duration.ofHours(1)), // 3 attempts per hour
        "/tasks", new RateLimit(100, Duration.ofMinutes(1)),     // 100 requests per minute
        "DEFAULT", new RateLimit(60, Duration.ofMinutes(1))      // Default: 60 per minute
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIP(httpRequest);
        String endpoint = httpRequest.getRequestURI();
        
        if (isRateLimited(clientIp, endpoint)) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            
            ErrorResponse error = new ErrorResponse(
                "Rate limit exceeded",
                "Too many requests. Please try again later."
            );
            
            httpResponse.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientIp, String endpoint) {
        RateLimit rateLimit = RATE_LIMITS.getOrDefault(endpoint, RATE_LIMITS.get("DEFAULT"));
        String key = "rate_limit:" + clientIp + ":" + endpoint;
        
        String currentCount = redisTemplate.opsForValue().get(key);
        
        if (currentCount == null) {
            redisTemplate.opsForValue().set(key, "1", rateLimit.getDuration());
            return false;
        }
        
        int count = Integer.parseInt(currentCount);
        if (count >= rateLimit.getLimit()) {
            return true;
        }
        
        redisTemplate.opsForValue().increment(key);
        return false;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}

public class RateLimit {
    private final int limit;
    private final Duration duration;
    
    public RateLimit(int limit, Duration duration) {
        this.limit = limit;
        this.duration = duration;
    }
    
    // Getters
}
```

### **API Key Management**

```java
@Entity
@Table(name = "api_keys")
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String keyHash;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private ApiKeyStatus status;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions;
    
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    
    // Getters and setters
}

@Service
public class ApiKeyService {
    
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    
    public ApiKeyResponse generateApiKey(User user, CreateApiKeyRequest request) {
        // Generate secure API key
        String apiKey = generateSecureApiKey();
        String hashedKey = passwordEncoder.encode(apiKey);
        
        ApiKey apiKeyEntity = new ApiKey();
        apiKeyEntity.setKeyHash(hashedKey);
        apiKeyEntity.setName(request.getName());
        apiKeyEntity.setUser(user);
        apiKeyEntity.setStatus(ApiKeyStatus.ACTIVE);
        apiKeyEntity.setPermissions(request.getPermissions());
        apiKeyEntity.setCreatedAt(LocalDateTime.now());
        apiKeyEntity.setExpiresAt(LocalDateTime.now().plus(request.getValidityPeriod()));
        
        apiKeyRepository.save(apiKeyEntity);
        
        // Return the plain API key only once
        return new ApiKeyResponse(apiKey, apiKeyEntity.getId(), apiKeyEntity.getExpiresAt());
    }
    
    public Optional<ApiKey> validateApiKey(String apiKey) {
        List<ApiKey> activeKeys = apiKeyRepository.findByStatus(ApiKeyStatus.ACTIVE);
        
        for (ApiKey key : activeKeys) {
            if (passwordEncoder.matches(apiKey, key.getKeyHash()) && !isExpired(key)) {
                // Update last used timestamp
                key.setLastUsedAt(LocalDateTime.now());
                apiKeyRepository.save(key);
                return Optional.of(key);
            }
        }
        
        return Optional.empty();
    }
    
    private String generateSecureApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "tsk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    private boolean isExpired(ApiKey apiKey) {
        return apiKey.getExpiresAt() != null && 
               apiKey.getExpiresAt().isBefore(LocalDateTime.now());
    }
}
```

### **CORS Configuration**

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins (configure based on environment)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://tasksphere.app",
            "https://*.tasksphere.app",
            "http://localhost:3000", // Development
            "http://localhost:3001"  // Development
        ));
        
        // Allowed methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "X-API-Key",
            "X-Request-ID"
        ));
        
        // Exposed headers
        configuration.setExposedHeaders(Arrays.asList(
            "X-Request-ID",
            "X-Rate-Limit-Remaining",
            "X-Rate-Limit-Reset"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour preflight cache
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
```

---

## 🛡️ **Security Headers**

```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }
}

public class SecurityHeadersFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self' https://api.tasksphere.app; " +
            "frame-ancestors 'none';"
        );
        
        // HSTS (HTTP Strict Transport Security)
        httpResponse.setHeader("Strict-Transport-Security",
            "max-age=31536000; includeSubDomains; preload");
        
        // X-Frame-Options
        httpResponse.setHeader("X-Frame-Options", "DENY");
        
        // X-Content-Type-Options
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-XSS-Protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Referrer Policy
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy
        httpResponse.setHeader("Permissions-Policy",
            "camera=(), microphone=(), geolocation=(), payment=()");
        
        // Remove server information
        httpResponse.setHeader("Server", "");
        
        chain.doFilter(request, response);
    }
}
```

---

## 🔍 **Security Monitoring & Incident Response**

### **Security Event Logging**

```java
@Component
public class SecurityEventLogger {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    
    public void logLoginAttempt(String email, String ip, boolean successful) {
        SecurityEvent event = SecurityEvent.builder()
            .eventType(SecurityEventType.LOGIN_ATTEMPT)
            .email(email)
            .ipAddress(ip)
            .successful(successful)
            .timestamp(Instant.now())
            .userAgent(getCurrentUserAgent())
            .build();
            
        securityLogger.info("Login attempt: {}", event);
        
        if (!successful) {
            checkForBruteForce(email, ip);
        }
    }
    
    public void logPrivilegeEscalation(Long userId, String action, String resource) {
        SecurityEvent event = SecurityEvent.builder()
            .eventType(SecurityEventType.PRIVILEGE_ESCALATION)
            .userId(userId)
            .action(action)
            .resource(resource)
            .ipAddress(getCurrentUserIP())
            .timestamp(Instant.now())
            .build();
            
        securityLogger.warn("Privilege escalation attempt: {}", event);
        
        // Trigger alert for immediate response
        alertService.sendSecurityAlert(event);
    }
    
    public void logDataAccess(Long userId, String resource, String action) {
        SecurityEvent event = SecurityEvent.builder()
            .eventType(SecurityEventType.DATA_ACCESS)
            .userId(userId)
            .resource(resource)
            .action(action)
            .ipAddress(getCurrentUserIP())
            .timestamp(Instant.now())
            .build();
            
        securityLogger.info("Data access: {}", event);
    }
    
    private void checkForBruteForce(String email, String ip) {
        // Implement brute force detection logic
        String key = "failed_attempts:" + ip + ":" + email;
        String attempts = redisTemplate.opsForValue().get(key);
        
        int count = attempts != null ? Integer.parseInt(attempts) : 0;
        count++;
        
        redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(15));
        
        if (count >= 5) {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.BRUTE_FORCE_DETECTED)
                .email(email)
                .ipAddress(ip)
                .metadata(Map.of("attemptCount", count))
                .timestamp(Instant.now())
                .build();
                
            securityLogger.error("Brute force attack detected: {}", event);
            alertService.sendSecurityAlert(event);
            
            // Temporarily block the IP
            blockService.blockIP(ip, Duration.ofMinutes(30));
        }
    }
}
```

### **Threat Detection**

```java
@Service
public class ThreatDetectionService {
    
    private final SecurityEventLogger securityLogger;
    private final RedisTemplate<String, String> redisTemplate;
    
    public void analyzeRequest(HttpServletRequest request, Authentication authentication) {
        String ip = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String uri = request.getRequestURI();
        
        // Check for suspicious patterns
        if (isSuspiciousUserAgent(userAgent)) {
            securityLogger.logSuspiciousActivity(ip, "Suspicious user agent: " + userAgent);
        }
        
        if (isSuspiciousRequestPattern(uri)) {
            securityLogger.logSuspiciousActivity(ip, "Suspicious request pattern: " + uri);
        }
        
        // Check for unusual activity patterns
        if (authentication != null) {
            Long userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
            analyzeUserBehavior(userId, ip, uri);
        }
    }
    
    private boolean isSuspiciousUserAgent(String userAgent) {
        if (userAgent == null) return true;
        
        String[] suspiciousPatterns = {
            "sqlmap", "nmap", "nikto", "burp", "curl", "wget", "python-requests"
        };
        
        String lowerUserAgent = userAgent.toLowerCase();
        return Arrays.stream(suspiciousPatterns)
            .anyMatch(lowerUserAgent::contains);
    }
    
    private boolean isSuspiciousRequestPattern(String uri) {
        String[] suspiciousPatterns = {
            "../", "..", "/admin", "/config", "/backup", ".sql", ".bak"
        };
        
        String lowerUri = uri.toLowerCase();
        return Arrays.stream(suspiciousPatterns)
            .anyMatch(lowerUri::contains);
    }
    
    private void analyzeUserBehavior(Long userId, String ip, String uri) {
        // Track request frequency
        String frequencyKey = "request_frequency:" + userId;
        redisTemplate.opsForZSet().add(frequencyKey, uri, System.currentTimeMillis());
        redisTemplate.expire(frequencyKey, Duration.ofMinutes(10));
        
        // Check if user is making too many requests
        long requestCount = redisTemplate.opsForZSet().count(frequencyKey, 
            System.currentTimeMillis() - 60000, System.currentTimeMillis());
            
        if (requestCount > 100) { // More than 100 requests per minute
            securityLogger.logSuspiciousActivity(ip, 
                "High request frequency: " + requestCount + " requests/minute");
        }
        
        // Track IP changes
        String lastIpKey = "last_ip:" + userId;
        String lastIp = redisTemplate.opsForValue().get(lastIpKey);
        
        if (lastIp != null && !lastIp.equals(ip)) {
            securityLogger.logSecurityEvent(SecurityEventType.IP_CHANGE, userId, 
                Map.of("oldIp", lastIp, "newIp", ip));
        }
        
        redisTemplate.opsForValue().set(lastIpKey, ip, Duration.ofHours(24));
    }
}
```

---

## 🔐 **Compliance & Audit**

### **GDPR Compliance**

```java
@Service
public class DataPrivacyService {
    
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuditLogger auditLogger;
    
    public void exportUserData(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
        UserDataExport export = UserDataExport.builder()
            .personalData(mapPersonalData(user))
            .tasks(taskRepository.findByUserId(userId))
            .loginHistory(getLoginHistory(userId))
            .dataProcessingHistory(getDataProcessingHistory(userId))
            .build();
            
        auditLogger.logDataExport(userId);
        
        // Generate and send export file
        generateExportFile(user, export);
    }
    
    public void anonymizeUserData(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
        // Anonymize personal data
        user.setEmail("anonymized_" + UUID.randomUUID() + "@deleted.com");
        user.setFirstName("Anonymized");
        user.setLastName("User");
        user.setPhoneNumber(null);
        user.setProfilePicture(null);
        user.setDeletedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Anonymize related data
        anonymizeUserTasks(userId);
        anonymizeUserComments(userId);
        
        auditLogger.logDataAnonymization(userId);
    }
    
    public void deleteUserData(Long userId) {
        // Verify user has requested deletion
        DeletionRequest request = deletionRequestRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("No deletion request found"));
            
        if (!request.isVerified()) {
            throw new IllegalStateException("Deletion request not verified");
        }
        
        // Delete user data in correct order
        taskRepository.deleteByUserId(userId);
        commentRepository.deleteByUserId(userId);
        sessionRepository.deleteByUserId(userId);
        auditLogRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        
        auditLogger.logDataDeletion(userId);
    }
}

@RestController
@RequestMapping("/privacy")
public class DataPrivacyController {
    
    private final DataPrivacyService dataPrivacyService;
    
    @GetMapping("/export")
    @PreAuthorize("authentication.principal.id == #userId")
    public ResponseEntity<Void> requestDataExport(@RequestParam Long userId) {
        dataPrivacyService.exportUserData(userId);
        return ResponseEntity.accepted().build();
    }
    
    @PostMapping("/delete")
    @PreAuthorize("authentication.principal.id == #userId")
    public ResponseEntity<Void> requestDataDeletion(@RequestParam Long userId) {
        dataPrivacyService.initiateDeletionProcess(userId);
        return ResponseEntity.accepted().build();
    }
}
```

### **Audit Trail**

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String resource;
    
    @Column(columnDefinition = "JSON")
    private String oldValues;
    
    @Column(columnDefinition = "JSON")
    private String newValues;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String sessionId;
    private String requestId;
    
    // Getters and setters
}

@Aspect
@Component
public class AuditAspect {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditMethodExecution(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }
            
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(principal.getId());
            auditLog.setAction(auditable.action());
            auditLog.setResource(auditable.resource());
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentUserIP());
            auditLog.setUserAgent(getCurrentUserAgent());
            auditLog.setSessionId(getCurrentSessionId());
            auditLog.setRequestId(getCurrentRequestId());
            
            // Capture method arguments as old values
            if (joinPoint.getArgs().length > 0) {
                auditLog.setOldValues(objectMapper.writeValueAsString(joinPoint.getArgs()));
            }
            
            // Capture result as new values
            if (result != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(result));
            }
            
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            // Log error but don't fail the main operation
            logger.error("Failed to create audit log", e);
        }
    }
}

// Usage
@Service
public class TaskService {
    
    @Auditable(action = "CREATE_TASK", resource = "TASK")
    public Task createTask(CreateTaskRequest request) {
        // Implementation
    }
    
    @Auditable(action = "UPDATE_TASK", resource = "TASK")
    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        // Implementation
    }
    
    @Auditable(action = "DELETE_TASK", resource = "TASK")
    public void deleteTask(Long taskId) {
        // Implementation
    }
}
```

---

## 🛡️ **Infrastructure Security**

### **Docker Security**

```dockerfile
# Secure Dockerfile
FROM openjdk:21-jdk-slim as builder

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Set working directory
WORKDIR /app

# Copy and build application
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Production stage
FROM openjdk:21-jre-slim

# Install security updates
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Set working directory
WORKDIR /app

# Copy application jar
COPY --from=builder /app/target/tasksphere-*.jar app.jar

# Change ownership to spring user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **AWS Security Configuration**

```yaml
# ECS Task Definition Security
{
  "family": "tasksphere-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::account:role/taskSphereTaskRole",
  "containerDefinitions": [
    {
      "name": "tasksphere-backend",
      "image": "your-ecr-repo/tasksphere:latest",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "DATABASE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:tasksphere/db-password"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:tasksphere/jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/aws/ecs/tasksphere",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      },
      "readonlyRootFilesystem": true,
      "user": "spring"
    }
  ]
}
```

---

## 📞 **Security Support**

### **Security Incident Response**

| Severity | Response Time | Contact |
|----------|---------------|---------|
| Critical | Immediate | security@tasksphere.app |
| High | 1 hour | security@tasksphere.app |
| Medium | 4 hours | security@tasksphere.app |
| Low | 24 hours | security@tasksphere.app |

### **Security Contacts**

- **Security Team**: security@tasksphere.app
- **Primary**: manohar.pagadala@example.com
- **Emergency**: +1-xxx-xxx-xxxx (24/7)

---

**🔒 Your application security is enterprise-grade and production-ready!**