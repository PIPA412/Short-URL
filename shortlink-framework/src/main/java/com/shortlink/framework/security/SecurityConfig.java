package com.shortlink.framework.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 * <p>
 * Configures a stateless JWT-based security model:
 * <ul>
 *   <li>CSRF disabled (stateless API, no cookies)</li>
 *   <li>Session management = STATELESS</li>
 *   <li>Public endpoints: auth (login/register), short code redirect, Swagger UI</li>
 *   <li>All other {@code /api/**} endpoints require authentication</li>
 * </ul>
 *
 * @author ShortLink
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security filter chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless JWT doesn't need it
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — no HttpSession, no JSESSIONID cookie
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Route authorization
            .authorizeHttpRequests(auth -> auth
                // === Public endpoints (no auth required) ===
                // Short code redirect: /{shortCode} (4–16 alphanumeric chars)
                .requestMatchers("/{shortCode:[a-zA-Z0-9]{4,16}}").permitAll()
                // Auth endpoints: login, register
                .requestMatchers("/api/auth/**").permitAll()
                // Swagger / SpringDoc
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**").permitAll()
                // Actuator health check (if used)
                .requestMatchers("/actuator/health").permitAll()

                // === Protected endpoints ===
                .anyRequest().authenticated()
            )

            // Insert JWT filter before Spring Security's standard auth filter
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose the AuthenticationManager as a bean (needed by AuthController for login).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
