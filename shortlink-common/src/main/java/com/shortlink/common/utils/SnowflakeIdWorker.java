package com.shortlink.common.utils;

/**
 * Twitter-Snowflake distributed ID generator.
 * <p>
 * Produces globally unique, time-sorted 64-bit IDs.
 * <p>
 * Bit layout (64 bits total):
 * <pre>
 *   1 bit  (unused sign)
 *  41 bits (timestamp delta from twepoch, ~69 years)
 *  10 bits (datacenter 5 + worker 5, 1024 combinations)
 *  12 bits (sequence number per ms, 4096/ms)
 * </pre>
 *
 * @author ShortLink
 */
public class SnowflakeIdWorker {

    // ==================== Configuration ====================

    /** Custom epoch: 2024-01-01 00:00:00 UTC (milliseconds) */
    private static final long TWEPOCH = 1704067200000L;

    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);       // 31
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS); // 31

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;                              // 12
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;          // 17
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS; // 22

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS); // 4095

    // ==================== Instance State ====================

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // ==================== Singleton ====================

    /** Default shared instance (workerId=1, datacenterId=0) */
    private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(1, 0);

    /**
     * Get the shared singleton instance.
     */
    public static SnowflakeIdWorker getInstance() {
        return INSTANCE;
    }

    /**
     * Generate the next unique ID using the shared instance.
     */
    public static long nextId() {
        return INSTANCE.nextIdInternal();
    }

    // ==================== Constructor ====================

    /**
     * Create a SnowflakeIdWorker with the given worker and datacenter IDs.
     *
     * @param workerId     worker ID (0–31)
     * @param datacenterId datacenter ID (0–31)
     * @throws IllegalArgumentException if IDs are out of range
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("workerId must be in [0, %d], got %d", MAX_WORKER_ID, workerId));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenterId must be in [0, %d], got %d", MAX_DATACENTER_ID, datacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ==================== Core Algorithm ====================

    /**
     * Generate the next unique ID (thread-safe).
     *
     * @return a 64-bit unique ID
     * @throws RuntimeException if the system clock moves backwards
     */
    public synchronized long nextIdInternal() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d ms",
                            lastTimestamp - timestamp));
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // Sequence exhausted this millisecond — spin until next ms
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - TWEPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * Spin-wait until the next millisecond.
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
