package com.tasksphere.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Tracks active WebSocket sessions for scaling and monitoring
 * Exposes metrics that can be used by auto-scaling policies
 */
@Component
@Slf4j
public class WebSocketSessionMetrics implements ApplicationListener<AbstractSubProtocolEvent> {

    private final AtomicInteger activeSessions = new AtomicInteger(0);

    public WebSocketSessionMetrics(MeterRegistry registry) {
        Gauge.builder("realtime.websocket.active", activeSessions, AtomicInteger::get)
             .description("Active STOMP WebSocket sessions")
             .register(registry);
    }

    @Override
    public void onApplicationEvent(@NonNull AbstractSubProtocolEvent event) {
        if (event instanceof SessionConnectEvent) {
            int current = activeSessions.incrementAndGet();
            log.debug("WebSocket session connected. Active sessions: {}", current);
        } else if (event instanceof SessionDisconnectEvent) {
            int current = activeSessions.decrementAndGet();
            log.debug("WebSocket session disconnected. Active sessions: {}", current);
        }
    }

    public int getActiveSessions() {
        return activeSessions.get();
    }
}