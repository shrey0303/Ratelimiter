package com.ratelimiter.limiter;

import com.ratelimiter.config.HierarchicalRateLimitProperties;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "hierarchical-rate-limits.global.limit-per-hour=5",
        "hierarchical-rate-limits.tenants.standard.limit-per-hour=3",
        "hierarchical-rate-limits.users.default-per-tenant=2"
})
@Testcontainers
public class HierarchicalRateLimiterServiceTest {
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private HierarchicalRateLimiterService service;

    private String tenantId;
    private String userId1;
    private String userId2;

    @BeforeEach
    void setup() {
        tenantId = "standard";
        userId1 = UUID.randomUUID().toString();
        userId2 = UUID.randomUUID().toString();
    }

    @Test
    void userQuotaExhaustion() {
        // User can make 2 requests, 3rd is denied at USER
        assertThat(service.isAllowed(tenantId, userId1).isAllowed()).isTrue();
        assertThat(service.isAllowed(tenantId, userId1).isAllowed()).isTrue();
        HierarchicalRateLimitResult denied = service.isAllowed(tenantId, userId1);
        assertThat(denied.isAllowed()).isFalse();
        assertThat(denied.getDeniedAt()).isEqualTo(HierarchicalRateLimitResult.Level.USER);
    }

    @Test
    void tenantQuotaExhaustion() {
        // 2 users, each can do 2, but tenant limit is 3, so 4th call (from either) is denied at TENANT
        assertThat(service.isAllowed(tenantId, userId1).isAllowed()).isTrue(); // 1
        assertThat(service.isAllowed(tenantId, userId2).isAllowed()).isTrue(); // 2
        assertThat(service.isAllowed(tenantId, userId1).isAllowed()).isTrue(); // 3
        HierarchicalRateLimitResult denied = service.isAllowed(tenantId, userId2); // 4th
        assertThat(denied.isAllowed()).isFalse();
        assertThat(denied.getDeniedAt()).isEqualTo(HierarchicalRateLimitResult.Level.TENANT);
    }
} 