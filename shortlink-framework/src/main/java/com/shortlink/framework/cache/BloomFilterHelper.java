package com.shortlink.framework.cache;

import com.shortlink.common.constant.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * Redis-backed Bloom Filter for cache penetration prevention.
 * <p>
 * A Bloom Filter is a probabilistic data structure that can definitively
 * say "this key does NOT exist" — which is exactly what we need to avoid
 * querying the database for non-existent short codes.
 * <p>
 * <b>How it works:</b>
 * <ol>
 *   <li>When a short link is created, its shortCode is added to the filter.</li>
 *   <li>On redirect, the filter is checked FIRST — if it says "no", the
 *       request is immediately rejected without touching the DB.</li>
 *   <li>The filter uses Redis BITMAP (SETBIT/GETBIT) for persistence.</li>
 * </ol>
 * <p>
 * <b>False positive rate:</b> ~1% with the default parameters
 * (m = 2^24 bits ≈ 2MB, k = 3 hash functions, n ≈ 1M entries).
 *
 * @author ShortLink
 */
@Slf4j
@Component
public class BloomFilterHelper {

    private final StringRedisTemplate redisTemplate;

    /** Bitmap size: 2^24 = 16,777,216 bits ≈ 2 MB */
    private static final int BITMAP_SIZE = 1 << 24;

    /** Number of hash functions */
    private static final int HASH_COUNT = 3;

    /** Seeds for each hash function (must be distinct) */
    private static final int[] SEEDS = {31, 37, 41};

    public BloomFilterHelper(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("BloomFilter initialized: size={} bits ({} MB), hashFunctions={}",
                BITMAP_SIZE, BITMAP_SIZE / 8 / 1024 / 1024, HASH_COUNT);
    }

    /**
     * Add a short code to the Bloom Filter.
     * Should be called whenever a new short link is created.
     */
    public void add(String shortCode) {
        int[] offsets = hash(shortCode);
        for (int offset : offsets) {
            redisTemplate.opsForValue()
                    .setBit(CacheConstants.BLOOM_FILTER_KEY, offset, true);
        }
        log.debug("BloomFilter: added shortCode={}", shortCode);
    }

    /**
     * Check if a short code MIGHT exist.
     * <p>
     * Returns {@code true} if the code MIGHT be in the set
     * (false positives are possible ~1%).
     * Returns {@code false} if the code DEFINITELY does NOT exist
     * (no false negatives).
     *
     * @return false → definitely not in DB (skip DB query)
     *         true  → might be in DB (proceed to cache/DB)
     */
    public boolean mightExist(String shortCode) {
        int[] offsets = hash(shortCode);
        for (int offset : offsets) {
            Boolean bit = redisTemplate.opsForValue()
                    .getBit(CacheConstants.BLOOM_FILTER_KEY, offset);
            if (Boolean.FALSE.equals(bit)) {
                // One bit is 0 → definitely not present
                return false;
            }
        }
        return true; // All bits are 1 → might be present
    }

    /**
     * Warm up the Bloom Filter with existing short codes.
     * Called during application startup.
     */
    public void warmUp(java.util.List<String> existingCodes) {
        log.info("BloomFilter: warming up with {} existing short codes...", existingCodes.size());
        for (String code : existingCodes) {
            add(code);
        }
        log.info("BloomFilter: warm-up complete");
    }

    // ==================== Hash Functions ====================

    /**
     * Generate k hash offsets for a given value using the double-hashing technique.
     * <pre>
     *   offset[i] = (hash1 + i * hash2) % BITMAP_SIZE
     * </pre>
     * This technique (Kirsch-Mitzenmacher) produces k independent hash values
     * from only two hash computations.
     */
    private int[] hash(String value) {
        int[] offsets = new int[HASH_COUNT];
        long hash1 = murmurHash64(value, SEEDS[0]);
        long hash2 = murmurHash64(value, SEEDS[1]);

        for (int i = 0; i < HASH_COUNT; i++) {
            long combined = hash1 + ((long) i * hash2);
            // Ensure non-negative and within bitmap range
            offsets[i] = (int) ((combined & Long.MAX_VALUE) % BITMAP_SIZE);
        }
        return offsets;
    }

    /**
     * Simple MurmurHash-inspired 64-bit hash with a seed.
     * Fast and has good distribution properties for Bloom filters.
     */
    private long murmurHash64(String value, int seed) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        long h = seed ^ (bytes.length * 0xC6A4A7935BD1E995L);

        int len = bytes.length;
        int i = 0;
        while (i + 8 <= len) {
            long k = getLongLE(bytes, i);
            k *= 0xC6A4A7935BD1E995L;
            k ^= k >>> 47;
            k *= 0xC6A4A7935BD1E995L;
            h ^= k;
            h *= 0xC6A4A7935BD1E995L;
            i += 8;
        }

        // Handle remaining bytes
        if (i < len) {
            long k = 0;
            int shift = 0;
            while (i < len) {
                k |= ((long) (bytes[i] & 0xFF)) << shift;
                shift += 8;
                i++;
            }
            k *= 0xC6A4A7935BD1E995L;
            k ^= k >>> 47;
            k *= 0xC6A4A7935BD1E995L;
            h ^= k;
        }

        h ^= bytes.length;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;

        return h;
    }

    private long getLongLE(byte[] b, int offset) {
        return ((long) (b[offset] & 0xFF))
                | ((long) (b[offset + 1] & 0xFF) << 8)
                | ((long) (b[offset + 2] & 0xFF) << 16)
                | ((long) (b[offset + 3] & 0xFF) << 24)
                | ((long) (b[offset + 4] & 0xFF) << 32)
                | ((long) (b[offset + 5] & 0xFF) << 40)
                | ((long) (b[offset + 6] & 0xFF) << 48)
                | ((long) (b[offset + 7] & 0xFF) << 56);
    }
}
