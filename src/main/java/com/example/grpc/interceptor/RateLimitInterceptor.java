package com.example.grpc.interceptor;

import io.grpc.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RateLimitInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> USER_ID_KEY = 
        Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final String DEFAULT_USER = "anonymous";

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Extract user ID from metadata or use default
        String userId = headers.get(USER_ID_KEY);
        if (userId == null || userId.isEmpty()) {
            userId = DEFAULT_USER;
        }

        // Check rate limit
        if (!rateLimiterService.tryAcquire(userId)) {
            // Record denied request metric
            recordMetric(call.getMethodDescriptor().getFullMethodName(), userId, false);
            
            // Abort with RESOURCE_EXHAUSTED status
            call.close(Status.RESOURCE_EXHAUSTED
                    .withDescription("Rate limit exceeded for user: " + userId),
                    new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        // Record allowed request metric
        recordMetric(call.getMethodDescriptor().getFullMethodName(), userId, true);

        // Proceed with the call
        return next.startCall(call, headers);
    }

    private void recordMetric(String methodName, String userId, boolean allowed) {
        meterRegistry.counter("grpc.rate_limit",
                Arrays.asList(
                    Tag.of("method", methodName),
                    Tag.of("user_id", userId),
                    Tag.of("allowed", String.valueOf(allowed))
                )).increment();
    }
} 