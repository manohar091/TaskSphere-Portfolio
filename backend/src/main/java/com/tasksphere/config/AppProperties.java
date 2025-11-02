package com.tasksphere.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for TaskSphere application
 * Manages all external configuration values with defaults
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private S3 s3 = new S3();
    private Security security = new Security();
    private Email email = new Email();

    @Data
    public static class Jwt {
        private String secret;
        private long expiration = 900000; // 15 minutes
        private long refreshExpiration = 604800000; // 7 days
        private String issuer = "tasksphere-api";
    }

    @Data
    public static class S3 {
        private String bucket;
        private String region = "us-east-1";
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class Security {
        private String corsAllowedOrigins = "http://localhost:3000,http://localhost:3001";
        private int rateLimitLoginAttempts = 5;
        private int rateLimitApiRequests = 100;
        private int lockoutMinutes = 15;
    }

    @Data
    public static class Email {
        private String smtpHost;
        private int smtpPort = 587;
        private String username;
        private String password;
        private boolean enabled = false;
    }
}