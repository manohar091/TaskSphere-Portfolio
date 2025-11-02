package com.tasksphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import com.tasksphere.service.RealtimeSubscriber;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;
    
    @Value("${spring.redis.port:6379}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String redisHost = host != null ? host : "localhost";
        return new LettuceConnectionFactory(redisHost, port);
    }

    /**
     * Redis message listener container for pub/sub
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RealtimeSubscriber realtimeSubscriber) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        
        // Null safety checks
        if (connectionFactory != null) {
            container.setConnectionFactory(connectionFactory);
        }
        
        if (realtimeSubscriber != null) {
            // Create listener adapter for our subscriber
            MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(realtimeSubscriber);
            
            // Subscribe to project, issue, and sprint channels with pattern matching
            container.addMessageListener(listenerAdapter, new ChannelTopic("project.*"));
            container.addMessageListener(listenerAdapter, new ChannelTopic("issue.*"));  
            container.addMessageListener(listenerAdapter, new ChannelTopic("sprint.*"));
        }
        
        return container;
    }
}