package com.tasksphere.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 🚀 Redis Cache Configuration for TaskSphere
 * 
 * This configuration enables Redis caching for performance optimization.
 * It includes custom serialization, cache managers, and TTL settings.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class RedisCacheConfig {

    /**
     * 🔧 Redis Template Configuration
     * Custom RedisTemplate with optimized serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serialization for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serialization for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * 🗂️ Cache Manager Configuration
     * Configures different cache regions with appropriate TTL
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // Default TTL: 10 minutes
            .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            // Cache configurations for different data types
            .withCacheConfiguration("users", 
                defaultConfig.entryTtl(Duration.ofMinutes(30))) // User data: 30 min
            .withCacheConfiguration("projects", 
                defaultConfig.entryTtl(Duration.ofMinutes(15))) // Project data: 15 min
            .withCacheConfiguration("tasks", 
                defaultConfig.entryTtl(Duration.ofMinutes(5)))  // Task data: 5 min
            .withCacheConfiguration("notifications", 
                defaultConfig.entryTtl(Duration.ofMinutes(2)))  // Notifications: 2 min
            .withCacheConfiguration("reports", 
                defaultConfig.entryTtl(Duration.ofHours(1)))    // Reports: 1 hour
            .withCacheConfiguration("statistics", 
                defaultConfig.entryTtl(Duration.ofMinutes(15))) // Statistics: 15 min
            .transactionAware() // Enable transaction support
            .build();
    }

    /**
     * 🎯 Custom Object Mapper for Redis Serialization
     * Optimized for cache performance
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Use activateDefaultTyping instead of deprecated enableDefaultTyping
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        return mapper;
    }
}