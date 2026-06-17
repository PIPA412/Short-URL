package com.shortlink.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Password encoding utility.
 * Wraps Spring Security's BCrypt implementation for convenient use
 * across modules that don't depend on spring-security directly.
 *
 * @author ShortLink
 */
public class PasswordEncoder {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * Hash a plain-text password using BCrypt.
     *
     * @param rawPassword the plain-text password
     * @return BCrypt hashed password (60 characters)
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * Verify a plain-text password against a BCrypt hash.
     *
     * @param rawPassword     the plain-text password to check
     * @param encodedPassword the stored BCrypt hash
     * @return {@code true} if the password matches
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    private PasswordEncoder() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }
}
