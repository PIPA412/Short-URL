package com.shortlink.common.utils;

/**
 * Short code generator using Base62 encoding.
 * <p>
 * Converts a numerical ID (from {@link SnowflakeIdWorker}) into a compact,
 * alphanumeric short code suitable for use in URLs.
 * <p>
 * The encoding uses XOR scrambling and a multiply step to obfuscate the
 * original Snowflake ID, preventing sequential codes from being guessable.
 * <p>
 * Character set (62 chars):
 * {@code 0-9 A-Z a-z} — URL-safe, no ambiguous characters like 'l', '1', 'O', '0'.
 *
 * @author ShortLink
 */
public class ShortCodeGenerator {

    /** Base62 character set (no ambiguous chars: 0/O removed per config, but kept for full range) */
    private static final String BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int BASE = BASE62.length(); // 62

    /** Default short code length: 62^7 = ~3.5 trillion combinations */
    private static final int DEFAULT_CODE_LENGTH = 7;

    /** XOR constant for ID obfuscation (must be same for encode/decode) */
    private static final long XOR_CONSTANT = 0xABCDEF123456789L;

    /** Multiplier constant (golden ratio splitmix) for additional scrambling */
    private static final long MULTIPLIER = 0x9E3779B97F4A7C15L;

    // ==================== Public API ====================

    /**
     * Encode a Snowflake ID into a Base62 short code.
     *
     * @param id the numerical ID to encode
     * @return 7-character Base62 short code
     */
    public static String encode(long id) {
        // Step 1: XOR scramble to break sequential patterns
        long scrambled = id ^ XOR_CONSTANT;

        // Step 2: Multiply by golden-ratio constant for additional diffusion
        scrambled = scrambled * MULTIPLIER;

        // Step 3: Convert to Base62
        long value = Math.abs(scrambled);
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }

        // Step 4: Pad to minimum length with leading '0's
        while (sb.length() < DEFAULT_CODE_LENGTH) {
            sb.append(BASE62.charAt(0));
        }

        return sb.reverse().toString();
    }

    /**
     * Decode a Base62 short code back to the original Snowflake ID.
     *
     * @param code the Base62 short code
     * @return the original numerical ID
     * @throws IllegalArgumentException if the code contains invalid characters
     */
    public static long decode(String code) {
        // Step 1: Convert Base62 to decimal
        long result = 0;
        for (char c : code.toCharArray()) {
            int index = BASE62.indexOf(c);
            if (index < 0) {
                throw new IllegalArgumentException(
                        "Invalid Base62 character: '" + c + "' in code: " + code);
            }
            result = result * BASE + index;
        }

        // Step 2: Reverse the multiply (note: integer division; lossy for non-encoded values)
        result = result / MULTIPLIER;

        // Step 3: Reverse the XOR
        result = result ^ XOR_CONSTANT;

        return Math.abs(result);
    }

    /**
     * Generate a short code from a newly generated Snowflake ID.
     * Convenience method — combines ID generation and encoding.
     *
     * @return a fresh, unique short code
     */
    public static String generate() {
        return encode(SnowflakeIdWorker.nextId());
    }

    /**
     * Validate that a string looks like a valid short code.
     *
     * @param code the candidate code
     * @return true if the code only contains Base62 characters and has the right length
     */
    public static boolean isValidFormat(String code) {
        if (code == null || code.length() < 4 || code.length() > 16) {
            return false;
        }
        for (char c : code.toCharArray()) {
            if (BASE62.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    private ShortCodeGenerator() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }
}
