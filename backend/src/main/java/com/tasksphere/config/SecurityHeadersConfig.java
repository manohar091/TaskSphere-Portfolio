package com.tasksphere.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security Headers Configuration
 * 
 * Implements comprehensive security headers for enterprise applications:
 * - Content Security Policy (CSP)
 * - X-Frame-Options (Clickjacking protection)
 * - X-Content-Type-Options (MIME type sniffing protection)
 * - X-XSS-Protection (XSS protection)
 * - Strict-Transport-Security (HTTPS enforcement)
 * - Referrer-Policy (Referrer information control)
 * - Permissions-Policy (Feature policy)
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain filterChain) throws ServletException, IOException {
                
                // Content Security Policy - Prevent XSS and data injection attacks
                response.setHeader("Content-Security-Policy", 
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://unpkg.com; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
                    "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                    "img-src 'self' data: https: blob:; " +
                    "connect-src 'self' ws: wss:; " +
                    "frame-ancestors 'none'; " +
                    "base-uri 'self'; " +
                    "form-action 'self'");
                
                // Clickjacking protection
                response.setHeader("X-Frame-Options", "DENY");
                
                // MIME type sniffing protection
                response.setHeader("X-Content-Type-Options", "nosniff");
                
                // XSS protection (legacy browsers)
                response.setHeader("X-XSS-Protection", "1; mode=block");
                
                // HTTPS enforcement (if using HTTPS)
                if (request.isSecure()) {
                    response.setHeader("Strict-Transport-Security", 
                        "max-age=31536000; includeSubDomains; preload");
                }
                
                // Referrer policy
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                
                // Permissions Policy (Feature Policy)
                response.setHeader("Permissions-Policy", 
                    "geolocation=(), " +
                    "microphone=(), " +
                    "camera=(), " +
                    "payment=(), " +
                    "usb=(), " +
                    "magnetometer=(), " +
                    "gyroscope=(), " +
                    "speaker=()");
                
                // Clear server information
                response.setHeader("Server", "TaskSphere");
                
                // Cache control for sensitive endpoints
                String requestURI = request.getRequestURI();
                if (requestURI.startsWith("/api/auth/") || 
                    requestURI.startsWith("/api/admin/") || 
                    requestURI.contains("/profile")) {
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }
}