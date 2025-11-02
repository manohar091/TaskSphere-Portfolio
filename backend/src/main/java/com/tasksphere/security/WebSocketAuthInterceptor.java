package com.tasksphere.security;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.tasksphere.domain.user.User;
import com.tasksphere.domain.user.UserRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket handshake interceptor for JWT authentication
 * Validates JWT token during WebSocket connection establishment
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenUtil jwt;
    private final UserRepository users;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = getToken(request);
            if (token == null) {
                log.warn("WebSocket connection attempt without token");
                return false;
            }

            // Validate JWT token
            Claims claims = jwt.validate(token);
            String email = claims.getSubject();
            
            // Load user from database
            User user = users.findByEmail(email).orElseThrow(() -> 
                new RuntimeException("User not found: " + email));
            
            // Store user in WebSocket session attributes
            attributes.put("user", user);
            attributes.put("userId", user.getId());
            
            log.info("WebSocket connection authenticated for user: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("WebSocket authentication failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do after handshake
    }

    /**
     * Extract JWT token from query parameter or Authorization header
     * @param request The HTTP request
     * @return JWT token or null if not found
     */
    private String getToken(ServerHttpRequest request) {
        // First, try to get token from query parameter: /ws?token=...
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6); // Remove "token=" prefix
                }
            }
        }
        
        // Second, try to get token from Authorization header
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7); // Remove "Bearer " prefix
            }
        }
        
        return null;
    }
}