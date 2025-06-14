package com.example.grpc.interceptor;

import com.example.grpc.service.RateLimiterService;
import io.grpc.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;
    private RateLimiterService rateLimiterService;
    private MeterRegistry meterRegistry;

    @Mock
    private ServerCall<Object, Object> serverCall;
    @Mock
    private Metadata metadata;
    @Mock
    private ServerCallHandler<Object, Object> next;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        rateLimiterService = new RateLimiterService();
        interceptor = new RateLimitInterceptor(rateLimiterService, meterRegistry);

        when(serverCall.getMethodDescriptor()).thenReturn(MethodDescriptor.newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName("test/Test")
                .build());
    }

    @Test
    void interceptCall_ShouldAllowRequest_WhenUnderLimit() {
        // Given
        String userId = "test-user";
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER), userId);

        // When
        ServerCall.Listener<Object> listener = interceptor.interceptCall(serverCall, headers, next);

        // Then
        assertNotNull(listener);
        verify(next).startCall(eq(serverCall), eq(headers));
        assertEquals(1, meterRegistry.get("grpc.rate_limit.allowed").counter().count());
    }

    @Test
    void interceptCall_ShouldRejectRequest_WhenOverLimit() {
        // Given
        String userId = "test-user";
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER), userId);

        // Exhaust the rate limit
        for (int i = 0; i < 11; i++) {
            rateLimiterService.tryAcquire(userId);
        }

        // When
        ServerCall.Listener<Object> listener = interceptor.interceptCall(serverCall, headers, next);

        // Then
        assertNotNull(listener);
        verify(serverCall).close(any(Status.class), any(Metadata.class));
        verify(next, never()).startCall(any(), any());
        assertEquals(1, meterRegistry.get("grpc.rate_limit.denied").counter().count());
    }

    @Test
    void interceptCall_ShouldUseDefaultUser_WhenNoUserIdProvided() {
        // Given
        Metadata headers = new Metadata();

        // When
        ServerCall.Listener<Object> listener = interceptor.interceptCall(serverCall, headers, next);

        // Then
        assertNotNull(listener);
        verify(next).startCall(eq(serverCall), eq(headers));
        assertEquals(1, meterRegistry.get("grpc.rate_limit.allowed").counter().count());
    }
} 