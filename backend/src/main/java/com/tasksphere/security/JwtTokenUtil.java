package com.tasksphere.security;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tasksphere.domain.user.User;
import com.tasksphere.domain.user.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final UserRepository userRepository;
    
    @Value("${app.jwt.secret}")
    private String secret;
    
    private static final long ACCESS_EXP_MS = 15 * 60 * 1000;   // 15 min
    private static final long REFRESH_EXP_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("uid", user.getId())
                .claim("name", user.getName())
                .claim("roles", List.of("DEV"))   // or actual roles
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP_MS))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP_MS))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public User validateAndGetUser(String token) {
        Claims claims = validate(token);
        String email = claims.getSubject();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean isTokenValid(String token) {
        try {
            validate(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = validate(token);
        return claims.getSubject();
    }
}