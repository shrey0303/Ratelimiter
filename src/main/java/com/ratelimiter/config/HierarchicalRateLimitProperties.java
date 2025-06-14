package com.ratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "hierarchical-rate-limits")
public class HierarchicalRateLimitProperties {
    private LimitSpec global;
    private Map<String, LimitSpec> tenants;
    private UserDefaults users;

    public static class LimitSpec {
        private long limitPerHour;
        public long getLimitPerHour() { return limitPerHour; }
        public void setLimitPerHour(long limitPerHour) { this.limitPerHour = limitPerHour; }
    }
    public static class UserDefaults {
        private long defaultPerTenant;
        public long getDefaultPerTenant() { return defaultPerTenant; }
        public void setDefaultPerTenant(long defaultPerTenant) { this.defaultPerTenant = defaultPerTenant; }
    }
    public LimitSpec getGlobal() { return global; }
    public void setGlobal(LimitSpec global) { this.global = global; }
    public Map<String, LimitSpec> getTenants() { return tenants; }
    public void setTenants(Map<String, LimitSpec> tenants) { this.tenants = tenants; }
    public UserDefaults getUsers() { return users; }
    public void setUsers(UserDefaults users) { this.users = users; }
} 