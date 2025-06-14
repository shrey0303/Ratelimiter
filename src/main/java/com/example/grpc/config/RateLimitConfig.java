package com.example.grpc.config;

import com.example.grpc.interceptor.RateLimitInterceptor;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }
} 