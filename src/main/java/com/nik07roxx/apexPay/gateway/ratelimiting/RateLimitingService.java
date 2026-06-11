package com.nik07roxx.apexPay.gateway.ratelimiting;

// 1. Core clean bucket imports (No more io.github.bucket4j)
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RateLimitingService {

    // Unique key namespace to avoid conflict with idempotency keys
    private static final String REDIS_KEY_PREFIX = "rate_limit:";
    private final ProxyManager<byte[]> proxyManager;

    // Injecting the low-level Lettuce RedisClient from Spring Data Redis
    public RateLimitingService(RedisClient redisClient) {
        // The Lettuce proxy manager internally serializes state using byte arrays
        this.proxyManager = LettuceBasedProxyManager.builderFor(redisClient).build();
    }

    /**
     * Tries to consume exactly 1 token for a given key.
     * @param identifier The unique user identifier (e.g., User ID or IP address)
     * @return true if a token was available and consumed; false otherwise.
     */
    public boolean tryConsume(String identifier) {
        String redisKey = REDIS_KEY_PREFIX + identifier;

        // Business rules: 10 max burst capacity, refills 2 tokens every single second
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillIntervally(2, Duration.ofSeconds(1))
                        .build())
                .build();

        // Convert key to bytes since Lettuce proxy managers operate on raw byte keys
        byte[] keyBytes = redisKey.getBytes();

        // Remote atomic evaluation execution inside Redis
        return proxyManager.builder().build(keyBytes, configuration).tryConsume(1);
    }
}
