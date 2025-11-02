package com.tasksphere.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.domain.user.UserRepository;
import com.tasksphere.dto.auth.UserBasic;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository users;

    @GetMapping
    public List<UserBasic> list() {
        return users.findAll().stream()
                .map(u -> new UserBasic(u.getId(), u.getName(), u.getEmail(), List.of()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserBasic> one(@PathVariable Long id) {
        return users.findById(id)
                .map(u -> new UserBasic(u.getId(), u.getName(), u.getEmail(), List.of()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}