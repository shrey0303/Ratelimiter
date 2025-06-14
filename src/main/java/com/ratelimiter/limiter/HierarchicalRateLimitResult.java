package com.ratelimiter.limiter;

public class HierarchicalRateLimitResult {
    public enum Level { GLOBAL, TENANT, USER, NONE }
    private final boolean allowed;
    private final Level deniedAt;
    private final long remainingTokensGlobal;
    private final long remainingTokensTenant;
    private final long remainingTokensUser;

    public HierarchicalRateLimitResult(boolean allowed, Level deniedAt, long remainingTokensGlobal, long remainingTokensTenant, long remainingTokensUser) {
        this.allowed = allowed;
        this.deniedAt = deniedAt;
        this.remainingTokensGlobal = remainingTokensGlobal;
        this.remainingTokensTenant = remainingTokensTenant;
        this.remainingTokensUser = remainingTokensUser;
    }
    public boolean isAllowed() { return allowed; }
    public Level getDeniedAt() { return deniedAt; }
    public long getRemainingTokensGlobal() { return remainingTokensGlobal; }
    public long getRemainingTokensTenant() { return remainingTokensTenant; }
    public long getRemainingTokensUser() { return remainingTokensUser; }
} 