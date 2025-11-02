package com.tasksphere.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,Object>> handleRuntime(RuntimeException ex){
        Map<String,Object> err = new LinkedHashMap<>();
        err.put("timestamp", Instant.now());
        err.put("status", 400);
        err.put("error", "Bad Request");
        err.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String,Object>> notFound(NoSuchElementException ex){
        Map<String,Object> err = new LinkedHashMap<>();
        err.put("timestamp", Instant.now());
        err.put("status", 404);
        err.put("error", "Not Found");
        err.put("message", ex.getMessage());
        return ResponseEntity.status(404).body(err);
    }
}