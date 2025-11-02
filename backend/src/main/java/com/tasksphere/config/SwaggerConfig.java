package com.tasksphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * 📚 Swagger/OpenAPI Configuration for TaskSphere
 * 
 * This configuration provides comprehensive API documentation
 * with authentication support and detailed endpoint descriptions.
 */
@Configuration
public class SwaggerConfig {

    /**
     * 📖 OpenAPI Configuration
     * Configures Swagger UI with authentication and metadata
     */
    @Bean
    public OpenAPI taskSphereOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TaskSphere API")
                .description("""
                    🚀 **TaskSphere Enterprise Project Management System**
                    
                    A comprehensive project management platform with real-time collaboration features.
                    
                    ## Features
                    - 👥 **User Management**: Authentication, authorization, and role-based access
                    - 📊 **Project Management**: Create, manage, and track projects
                    - ✅ **Task Management**: Assign, track, and complete tasks
                    - 📈 **Performance Monitoring**: Real-time metrics and caching
                    - 🔔 **Real-time Notifications**: WebSocket-based live updates
                    - 📁 **File Management**: Upload and manage project attachments
                    
                    ## Authentication
                    Use the `/api/auth/login` endpoint to obtain a JWT token, then include it in the
                    `Authorization` header as `Bearer <token>` for authenticated requests.
                    
                    ## Monitoring
                    - Health checks available at `/actuator/health`
                    - Metrics available at `/actuator/metrics`
                    - Prometheus metrics at `/actuator/prometheus`
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("TaskSphere Development Team")
                    .email("support@tasksphere.app")
                    .url("https://tasksphere.app"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addServersItem(new Server()
                .url("http://localhost:8081")
                .description("Development Server"))
            .addServersItem(new Server()
                .url("https://api.tasksphere.app")
                .description("Production Server"))
            // Add JWT Security Scheme
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")))
            // Apply security globally
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}