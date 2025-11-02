package com.tasksphere.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for enabling scheduled tasks
 * Used for outbox event publishing and other background jobs
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}