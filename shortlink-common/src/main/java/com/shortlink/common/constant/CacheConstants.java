package com.shortlink.common.constant;

/**
 * Redis cache key constants and TTL values.
 * <p>
 * Each prefix is appended with a unique identifier to form the full cache key.
 *
 * @author ShortLink
 */
public class CacheConstants {

    // ==================== Key Prefixes ====================

    /** Short code → Original URL mapping: short:code:{shortCode} */
    public static final String SHORT_CODE_KEY = "short:code:";

    /** JWT token blacklist (v2 feature): token:blacklist:{tokenId} */
    public static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    /** User info cache: user:info:{userId} */
    public static final String USER_INFO_KEY = "user:info:";

    /** Click count cache (for async persistence): short:clicks:{shortCode} */
    public static final String CLICK_COUNT_KEY = "short:clicks:";

    /** Rate limiter key: rate:limit:{ip} */
    public static final String RATE_LIMIT_KEY = "rate:limit:";

    /** Short code generation lock: short:lock:{code} */
    public static final String SHORT_CODE_LOCK = "short:lock:";

    /** Bloom filter key for short codes: short:bloom */
    public static final String BLOOM_FILTER_KEY = "short:bloom";

    // ==================== Null Value Marker ====================

    /**
     * Cached value for non-existent short codes (cache penetration guard).
     * Any value EXCEPT this string is treated as a valid URL.
     * TTL: 5 minutes (configured in CacheConfig:short-link-null).
     */
    public static final String NULL_MARKER = "\0__NULL__";

    /**
     * Check if a cached value is the NULL_MARKER (meaning the short code
     * does not exist in the database).
     */
    public static boolean isNullMarker(String value) {
        return NULL_MARKER.equals(value);
    }

    // ==================== TTL Constants (seconds) ====================

    /** Short code → URL cache TTL: 24 hours */
    public static final long CODE_CACHE_TTL_HOURS = 24;

    /** Null marker cache TTL: 5 minutes */
    public static final long NULL_CACHE_TTL_MINUTES = 5;

    /** Pre-warmed cache TTL: 12 hours */
    public static final long WARM_CACHE_TTL_HOURS = 12;

    private CacheConstants() {
        throw new IllegalStateException("Constant class - do not instantiate");
    }
}
