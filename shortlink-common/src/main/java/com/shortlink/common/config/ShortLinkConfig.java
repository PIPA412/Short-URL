package com.shortlink.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application configuration properties for short link behavior.
 * <p>
 * Bound to the {@code short-link} YAML namespace.
 *
 * @author ShortLink
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link")
public class ShortLinkConfig {

    /**
     * Default number of days before a short link expires.
     * Used when the user does not specify an expiration date.
     * Default: 365 days (1 year).
     */
    private int defaultExpireDays = 365;

    /**
     * Length of the generated short code (Base62 characters).
     * Default: 7 (62^7 = ~3.5 trillion combinations).
     */
    private int codeLength = 7;

    /**
     * Base URL for constructing full short URLs.
     * Example: {@code http://localhost:8080}.
     * The short code is appended directly after this base URL.
     */
    private String baseUrl = "http://localhost:8080";
}
