package com.tasksphere.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tasksphere.domain.user.User;
import com.tasksphere.domain.user.UserRepository;
import com.tasksphere.dto.auth.AuthResponse;
import com.tasksphere.dto.auth.LoginRequest;
import com.tasksphere.dto.auth.SignupRequest;
import com.tasksphere.dto.auth.UserBasic;
import com.tasksphere.security.JwtTokenUtil;
import com.tasksphere.security.RateLimiter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtTokenUtil jwt;
    private final RateLimiter rateLimiter;
    private final ActivityService activityService;

    public UserBasic signup(SignupRequest req) {
        if (users.findByEmail(req.getEmail()).isPresent())
            throw new RuntimeException("Email already exists");
        
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        users.save(u);
        
        // Log successful registration
        activityService.logAuthEvent("REGISTRATION_SUCCESS", req.getEmail(), u.getId(), "localhost");
        
        return new UserBasic(u.getId(), u.getName(), u.getEmail(), List.of("DEV"));
    }

    public AuthResponse login(LoginRequest req) {
        // Check rate limiting before attempting login
        rateLimiter.checkLoginAttempts(req.getEmail());
        
        try {
            User u = users.findByEmail(req.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email"));
            
            if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
                // Log failed login attempt
                activityService.logAuthEvent("LOGIN_FAILURE", req.getEmail(), null, "localhost");
                throw new RuntimeException("Invalid password");
            }
            
            // Reset rate limiter on successful login
            rateLimiter.resetLoginAttempts(req.getEmail());
            
            String access = jwt.generateAccessToken(u);
            String refresh = jwt.generateRefreshToken(u);
            
            // Log successful login
            activityService.logAuthEvent("LOGIN_SUCCESS", req.getEmail(), u.getId(), "localhost");
            
            return new AuthResponse(access, refresh,
                    new UserBasic(u.getId(), u.getName(), u.getEmail(), List.of("DEV")));
                    
        } catch (RuntimeException e) {
            // Log failed login attempt
            activityService.logAuthEvent("LOGIN_FAILURE", req.getEmail(), null, "localhost");
            throw e;
        }
    }

    public AuthResponse refresh(String token) {
        User u = jwt.validateAndGetUser(token);
        String access = jwt.generateAccessToken(u);
        
        // Log token refresh
        activityService.logAuthEvent("TOKEN_REFRESH", u.getEmail(), u.getId(), "localhost");
        
        return new AuthResponse(access, token,
                new UserBasic(u.getId(), u.getName(), u.getEmail(), List.of("DEV")));
    }

    public UserBasic getCurrentUser(Authentication auth) {
        // For now, we'll get from token - later we'll implement proper auth
        User u = (User) auth.getPrincipal();
        return new UserBasic(u.getId(), u.getName(), u.getEmail(), List.of("DEV"));
    }
}