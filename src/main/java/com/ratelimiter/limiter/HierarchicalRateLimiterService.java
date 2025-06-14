package com.ratelimiter.limiter;

import com.ratelimiter.config.HierarchicalRateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class HierarchicalRateLimiterService {
    private final HierarchicalRateLimitProperties props;
    private final ProxyManager<UUID> proxyManager;

    public HierarchicalRateLimiterService(HierarchicalRateLimitProperties props, ProxyManager<UUID> proxyManager) {
        this.props = props;
        this.proxyManager = proxyManager;
    }

    private Bucket resolveBucket(UUID key, long limitPerHour) {
        Bandwidth limit = Bandwidth.classic(limitPerHour, Refill.greedy(limitPerHour, Duration.ofHours(1)));
        return proxyManager.builder().build(key, () -> io.github.bucket4j.BucketConfiguration.builder().addLimit(limit).build());
    }

    public HierarchicalRateLimitResult isAllowed(String tenantId, String userId) {
        // 1. Global
        UUID globalKey = UUID.nameUUIDFromBytes("ratelimit:global".getBytes());
        long globalLimit = props.getGlobal().getLimitPerHour();
        Bucket globalBucket = resolveBucket(globalKey, globalLimit);
        boolean globalAllowed = globalBucket.tryConsume(1);
        long remainingGlobal = globalBucket.getAvailableTokens();
        if (!globalAllowed) {
            return new HierarchicalRateLimitResult(false, HierarchicalRateLimitResult.Level.GLOBAL, remainingGlobal, -1, -1);
        }
        // 2. Tenant
        String tenantKeyStr = "ratelimit:tenant:" + tenantId;
        UUID tenantKey = UUID.nameUUIDFromBytes(tenantKeyStr.getBytes());
        Map<String, HierarchicalRateLimitProperties.LimitSpec> tenants = props.getTenants();
        long tenantLimit = tenants != null && tenants.containsKey(tenantId) ? tenants.get(tenantId).getLimitPerHour() : globalLimit;
        Bucket tenantBucket = resolveBucket(tenantKey, tenantLimit);
        boolean tenantAllowed = tenantBucket.tryConsume(1);
        long remainingTenant = tenantBucket.getAvailableTokens();
        if (!tenantAllowed) {
            return new HierarchicalRateLimitResult(false, HierarchicalRateLimitResult.Level.TENANT, remainingGlobal, remainingTenant, -1);
        }
        // 3. User
        String userKeyStr = "ratelimit:user:" + userId;
        UUID userKey = UUID.nameUUIDFromBytes(userKeyStr.getBytes());
        long userLimit = props.getUsers().getDefaultPerTenant();
        Bucket userBucket = resolveBucket(userKey, userLimit);
        boolean userAllowed = userBucket.tryConsume(1);
        long remainingUser = userBucket.getAvailableTokens();
        if (!userAllowed) {
            return new HierarchicalRateLimitResult(false, HierarchicalRateLimitResult.Level.USER, remainingGlobal, remainingTenant, remainingUser);
        }
        // All allowed
        return new HierarchicalRateLimitResult(true, HierarchicalRateLimitResult.Level.NONE, remainingGlobal, remainingTenant, remainingUser);
    }
} 