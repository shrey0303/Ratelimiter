package com.example.grpc.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static final int DEFAULT_TOKENS = 10;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    public boolean tryAcquire(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, this::createNewBucket);
        return bucket.tryConsume(1);
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(DEFAULT_TOKENS, 
            Refill.greedy(DEFAULT_TOKENS, REFILL_DURATION));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
} 