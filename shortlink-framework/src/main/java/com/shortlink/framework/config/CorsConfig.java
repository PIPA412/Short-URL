package com.shortlink.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS (Cross-Origin Resource Sharing) configuration.
 * <p>
 * Allows the Vue 3 dev server (localhost:5173) to call the backend APIs
 * without triggering same-origin policy restrictions.
 * <p>
 * In production, restrict {@code allowedOrigins} to the actual frontend domain.
 *
 * @author ShortLink
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow the Vite dev server origin
        config.addAllowedOriginPattern("http://localhost:*");
        // Allow all headers and methods for development
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // Allow credentials (cookies, Authorization header)
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
