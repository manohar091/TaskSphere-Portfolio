package com.tasksphere.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tasksphere.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/health/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                
                // Actuator endpoints (monitoring & health checks)
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics/**").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/env").permitAll()
                .requestMatchers("/actuator/loggers/**").permitAll()
                .requestMatchers("/actuator/threaddump").permitAll()
                .requestMatchers("/actuator/heapdump").permitAll()
                
                // Performance monitoring endpoints
                .requestMatchers("/api/performance/**").permitAll()
                
                // Swagger/OpenAPI Documentation
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/*/role").hasRole("ADMIN")
                .requestMatchers("/api/users/create").hasRole("ADMIN")
                
                // Project Manager endpoints
                .requestMatchers("/api/projects/create").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                .requestMatchers("/api/projects/*/delete").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                .requestMatchers("/api/projects/*/members").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                .requestMatchers("/api/sprints/create").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                .requestMatchers("/api/sprints/*/delete").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                
                // User management endpoints
                .requestMatchers("/api/users/profile").authenticated()
                .requestMatchers("/api/users/*/profile").authenticated()
                
                // Project endpoints (authenticated users)
                .requestMatchers("/api/projects/**").authenticated()
                .requestMatchers("/api/sprints/**").authenticated()
                .requestMatchers("/api/issues/**").authenticated()
                .requestMatchers("/api/comments/**").authenticated()
                .requestMatchers("/api/attachments/**").authenticated()
                .requestMatchers("/api/activity/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow specific origins for production security
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",    // React dev server
            "http://localhost:3001",    // Alternative dev port
            "https://tasksphere.app",   // Production frontend
            "https://www.tasksphere.app" // Production www subdomain
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}