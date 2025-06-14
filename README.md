# Rate-Limited gRPC Microservice

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A robust, production-ready gRPC microservice template with built-in hierarchical rate limiting capabilities, built using Spring Boot and Bucket4j.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Technical Stack](#technical-stack)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Rate Limiting](#rate-limiting)
- [Hierarchical Quotas](#hierarchical-quotas)
- [gRPC Interceptors](#grpc-interceptors)
- [Metrics and Monitoring](#metrics-and-monitoring)
- [Testing](#testing)
- [Client Implementation](#client-implementation)
- [Security](#security)
- [Performance Considerations](#performance-considerations)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

This project implements a gRPC-based microservice with sophisticated hierarchical rate limiting capabilities. It serves as a template for building scalable, resilient microservices that need to handle high-throughput scenarios while maintaining service quality through multi-level rate limiting.

## Architecture

The service follows a layered architecture:

1. **Transport Layer**
   - gRPC server implementation
   - HTTP/2 based communication
   - Metadata handling for authentication
   - Interceptor chain for request processing

2. **Service Layer**
   - Business logic implementation
   - Hierarchical rate limiting enforcement
   - Request validation
   - Tenant and user management

3. **Storage Layer**
   - Redis-based distributed rate limiting
   - JCache implementation
   - Rate limit state management
   - Distributed token bucket storage

4. **Monitoring Layer**
   - Prometheus metrics
   - Actuator endpoints
   - Health checks
   - Rate limit statistics

## Features

### Core Features
- gRPC service implementation with Spring Boot
- Hierarchical token bucket-based rate limiting using Bucket4j
- Redis-backed distributed rate limiting
- Prometheus metrics integration
- Comprehensive unit and integration tests
- Example client implementation
- Health check endpoints
- Configurable rate limits
- User and tenant-based rate limiting
- Metrics for monitoring and alerting

### Advanced Features
- Hierarchical rate limiting with multiple levels:
  - Global service limits
  - Tenant-level quotas
  - User-level quotas
  - Endpoint-specific limits
- Customizable rate limit strategies
- Graceful degradation
- Circuit breaker pattern support
- Request tracing
- Performance monitoring
- Dynamic quota adjustment
- Redis-based distributed rate limiting
- JCache implementation for distributed storage

## Technical Stack

- **Java 17+**: Modern Java features and performance
- **Spring Boot 3.2.3**: Application framework
- **gRPC**: High-performance RPC framework
- **Bucket4j**: Rate limiting implementation
- **Redis**: Distributed rate limit storage
- **JCache**: Distributed caching implementation
- **Micrometer**: Metrics collection
- **Prometheus**: Metrics storage and visualization
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **Maven**: Build and dependency management

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── grpc/
│   │               ├── Application.java
│   │               ├── config/
│   │               │   └── RateLimitConfig.java
│   │               ├── interceptor/
│   │               │   └── RateLimitInterceptor.java
│   │               ├── service/
│   │               │   ├── RateLimiterService.java
│   │               │   ├── HierarchicalRateLimiterService.java
│   │               │   └── UserServiceImpl.java
│   │               └── client/
│   │                   └── UserServiceClient.java
│   ├── proto/
│   │   └── user_service.proto
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── com/
            └── example/
                └── grpc/
                    ├── service/
                    │   ├── UserServiceImplTest.java
                    │   └── HierarchicalRateLimiterServiceTest.java
                    └── interceptor/
                        └── RateLimitInterceptorTest.java
```

## Hierarchical Quotas

### Quota Levels
1. **Global Service Level**
   - Overall service capacity
   - Protects against system overload
   - Configurable through application properties
   - Default: 100,000 requests per hour

2. **Tenant Level**
   - Per-tenant quotas
   - Fair resource distribution
   - Tenant isolation
   - Configurable per tenant:
     - Premium: 50,000 requests per hour
     - Standard: 10,000 requests per hour

3. **User Level**
   - Individual user limits
   - User-specific quotas
   - Personalized rate limits
   - Default: 1,000 requests per hour per tenant

### Quota Configuration
```yaml
hierarchical-rate-limits:
  global:
    limit-per-hour: 100000
  tenants:
    premium:
      limit-per-hour: 50000
    standard:
      limit-per-hour: 10000
  users:
    default-per-tenant: 1000
```

### Quota Enforcement
- Hierarchical checking with fail-fast approach
- Redis-backed distributed rate limiting
- JCache implementation for distributed storage
- Graceful degradation
- Quota exhaustion handling

## gRPC Interceptors

### RateLimitInterceptor
```java
@Component
public class RateLimitInterceptor implements ServerInterceptor {
    private static final Metadata.Key<String> USER_ID_KEY = 
        Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final String DEFAULT_USER = "anonymous";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String userId = headers.get(USER_ID_KEY);
        if (userId == null || userId.isEmpty()) {
            userId = DEFAULT_USER;
        }

        if (!rateLimiterService.tryAcquire(userId)) {
            recordMetric(call.getMethodDescriptor().getFullMethodName(), userId, false);
            call.close(Status.RESOURCE_EXHAUSTED
                    .withDescription("Rate limit exceeded for user: " + userId),
                    new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        recordMetric(call.getMethodDescriptor().getFullMethodName(), userId, true);
        return next.startCall(call, headers);
    }
}
```

### Interceptor Chain
1. Authentication Interceptor
2. Rate Limit Interceptor
3. Logging Interceptor
4. Metrics Interceptor
5. Business Logic

## Rate Limiting

### Implementation Details
- Token bucket algorithm using Bucket4j
- Redis-backed distributed rate limiting
- JCache implementation for distributed storage
- Hierarchical rate limiting
- Configurable rate limits per level
- Support for burst handling
- Graceful degradation

### Rate Limit Headers
- `user-id`: Required for user-based rate limiting
- `tenant-id`: Required for tenant-based rate limiting
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Remaining requests
- `X-RateLimit-Reset`: Time until rate limit reset
- `X-RateLimit-Level`: Current limiting level (global/tenant/user)

### Rate Limit Response
```protobuf
message RateLimitResponse {
  bool allowed = 1;
  string message = 2;
  int32 remaining_requests = 3;
  int64 reset_time = 4;
  string limiting_level = 5;
}
```

## Redis Configuration

### Cache Manager Setup
```java
@Configuration
public class RedisConfiguration {
    private static final String CACHE_NAME = "rate-limit";

    @Bean(name = "rate-limit-cache-manager")
    public CacheManager cacheManager(final RedisProperties redisProperties) {
        final var cacheManager = Caching.getCachingProvider().getCacheManager();
        final var isCacheCreated = Optional.ofNullable(cacheManager.getCache(CACHE_NAME)).isPresent();
        
        if (Boolean.FALSE.equals(isCacheCreated)) {
            final var connectionUrl = String.format("redis://%s:%d", 
                redisProperties.getHost(), 
                redisProperties.getPort());
            final var configuration = new Config();
            configuration.useSingleServer()
                .setPassword(redisProperties.getPassword())
                .setAddress(connectionUrl);

            cacheManager.createCache(CACHE_NAME, 
                RedissonConfiguration.fromConfig(configuration));
        }
        return cacheManager;
    }

    @Bean
    ProxyManager<UUID> proxyManager(final CacheManager cacheManager) {
        return new JCacheProxyManager<UUID>(cacheManager.getCache(CACHE_NAME));
    }
}
```

## Metrics and Monitoring

### Available Metrics
- `grpc.rate_limit.allowed`: Counter of allowed requests
- `grpc.rate_limit.denied`: Counter of rate-limited requests
- `grpc.rate_limit.by_level`: Counter by limiting level
- `grpc.requests.total`: Total number of requests
- `grpc.requests.latency`: Request latency
- `grpc.rate_limit.remaining`: Gauge of remaining quota
- `grpc.rate_limit.exhausted`: Counter of quota exhaustion events

### Prometheus Integration
- Metrics exposed at `/actuator/prometheus`
- Configurable scrape interval
- Support for custom metrics
- Alert rules for quota exhaustion

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test
```

### Integration Tests
```bash
# Run integration tests
mvn verify
```

### Rate Limit Tests
```java
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
    // 2 users, each can do 2, but tenant limit is 3, so 4th call is denied at TENANT
    assertThat(service.isAllowed(tenantId, userId1).isAllowed()).isTrue(); // 1
    assertThat(service.isAllowed(tenantId, userId2).isAllowed()).isTrue(); // 2
    assertThat(service.isAllowed(tenantId, userId1).isAllowed()).isTrue(); // 3
    HierarchicalRateLimitResult denied = service.isAllowed(tenantId, userId2); // 4th
    assertThat(denied.isAllowed()).isFalse();
    assertThat(denied.getDeniedAt()).isEqualTo(HierarchicalRateLimitResult.Level.TENANT);
}
```

## Client Implementation

### Example Client
```java
UserServiceClient client = new UserServiceClient("localhost", 9090);
try {
    // Set rate limit headers
    Metadata headers = new Metadata();
    headers.put(USER_ID_KEY, "user1");
    headers.put(TENANT_ID_KEY, "tenant1");
    
    // Get user with rate limit headers
    GetUserResponse user = client.getUser("test-user", headers);
    
    // Update user with rate limit headers
    UpdateUserResponse updateResponse = client.updateUser(
        "test-user", 
        "Updated Name", 
        "updated@example.com",
        headers
    );
} finally {
    client.shutdown();
}
```

## Security

### Authentication
- JWT-based authentication
- Metadata-based user identification
- Role-based access control
- Tenant isolation

### Rate Limiting Security
- Protection against DoS attacks
- IP-based rate limiting
- Tenant isolation
- Quota enforcement
- Rate limit bypass protection

## Performance Considerations

### Optimization Tips
- Use connection pooling
- Implement caching where appropriate
- Monitor memory usage
- Configure appropriate thread pools
- Optimize rate limit checks
- Use efficient data structures
- Redis connection pooling
- JCache optimization

### Scaling
- Horizontal scaling support
- Load balancing ready
- Stateless design
- Distributed rate limiting
- Quota synchronization
- Redis cluster support

## Troubleshooting

### Common Issues
1. Rate limit exceeded
   - Check current usage
   - Verify rate limit configuration
   - Monitor metrics
   - Check quota levels
   - Verify Redis connectivity

2. Connection issues
   - Verify server is running
   - Check port availability
   - Validate client configuration
   - Check network connectivity
   - Verify Redis connection

### Debugging
- Enable debug logging
- Check Prometheus metrics
- Monitor system resources
- Analyze rate limit logs
- Check quota exhaustion events
- Monitor Redis performance

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License - See LICENSE file for details

## Setup and Installation

### Environment Configuration
The application uses environment variables for configuration. You can either:

1. Use the default development credentials (not recommended for production):
```yaml
# Default development credentials
MYSQL_ROOT_PASSWORD: Password@123
MYSQL_USER: ratemaster
MYSQL_PASSWORD: Password@123
REDIS_PASSWORD: Password@123
JWT_SECRET_KEY: 093617ebfa4b9af9700db274ac204ffa34195494d97b9c26c23ad561de817926
```

2. Set your own credentials using environment variables:
```bash
# Set your own credentials
export MYSQL_ROOT_PASSWORD=your_secure_password
export MYSQL_USER=your_mysql_user
export MYSQL_PASSWORD=your_mysql_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET_KEY=your_jwt_secret

# Then run docker-compose
docker-compose up
```

Or create a `.env` file in the project root:
```env
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_USER=your_mysql_user
MYSQL_PASSWORD=your_mysql_password
REDIS_PASSWORD=your_redis_password
JWT_SECRET_KEY=your_jwt_secret
```

For production environments, it's recommended to:
1. Use strong, unique passwords
2. Store credentials in a secure secrets management system
3. Rotate passwords regularly
4. Use different passwords for different environments