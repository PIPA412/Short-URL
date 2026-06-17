package com.shortlink.framework.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Cache configuration backed by Redis.
 * <p>
 * Enables {@code @Cacheable}, {@code @CacheEvict}, {@code @CachePut}, and
 * {@code @Caching} annotations across the application.
 * <p>
 * Defines per-cache TTL (time-to-live) policies:
 * <table>
 *   <tr><th>Cache Name</th><th>TTL</th><th>Usage</th></tr>
 *   <tr><td>{@code short-link}</td><td>24 hours</td><td>ShortLink entities by shortCode</td></tr>
 *   <tr><td>{@code short-link-id}</td><td>1 hour</td><td>ShortLink entities by ID</td></tr>
 *   <tr><td>{@code short-link-null}</td><td>5 minutes</td><td>Null value markers (penetration guard)</td></tr>
 *   <tr><td>{@code short-link-warm}</td><td>12 hours</td><td>Pre-warmed hot links</td></tr>
 * </table>
 *
 * @author ShortLink
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Default TTL for caches without explicit configuration */
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    // ---- Per-Cache TTL Definitions ----

    private static final Duration SHORT_LINK_TTL      = Duration.ofHours(24);
    private static final Duration SHORT_LINK_ID_TTL   = Duration.ofHours(1);
    private static final Duration SHORT_LINK_NULL_TTL = Duration.ofMinutes(5);
    private static final Duration SHORT_LINK_WARM_TTL = Duration.ofHours(12);

    /**
     * Primary CacheManager backed by Redis.
     * Each cache name maps to a dedicated Redis hash namespace with its own TTL.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Base configuration: string keys, JSON values, no null caching by default
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();  // null handled explicitly per cache

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("short-link",      defaultConfig.entryTtl(SHORT_LINK_TTL));
        cacheConfigs.put("short-link-id",   defaultConfig.entryTtl(SHORT_LINK_ID_TTL));
        cacheConfigs.put("short-link-null", defaultConfig.entryTtl(SHORT_LINK_NULL_TTL));
        cacheConfigs.put("short-link-warm", defaultConfig.entryTtl(SHORT_LINK_WARM_TTL));

        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
