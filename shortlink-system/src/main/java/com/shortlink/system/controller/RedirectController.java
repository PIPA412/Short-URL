package com.shortlink.system.controller;

import com.shortlink.common.constant.CacheConstants;
import com.shortlink.common.constant.Constants;
import com.shortlink.system.service.ShortLinkService;
import com.shortlink.system.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Redirect controller — the core redirect endpoint.
 * <p>
 * This is a {@code @Controller} (not {@code @RestController}) because it
 * returns a 302 redirect response, not JSON.
 * <p>
 * The endpoint {@code GET /{shortCode}} is publicly accessible — no
 * authentication is required. Spring Security is configured to permit
 * requests matching the pattern {@code /{shortCode:[a-zA-Z0-9]{4,16}}}.
 *
 * @author ShortLink
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final StringRedisTemplate redisTemplate;
    private final ShortLinkService shortLinkService;
    private final StatsService statsService;

    @Operation(summary = "Short link redirect",
            description = "Resolve a short code and redirect to the original URL (302)")
    @GetMapping("/{shortCode:" + Constants.SHORT_CODE_PATTERN + "}")
    public void redirect(@PathVariable String shortCode,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        // 1. Try Redis cache first (fast path for hot links)
        String cacheKey = CacheConstants.SHORT_CODE_KEY + shortCode;
        String originalUrl = redisTemplate.opsForValue().get(cacheKey);

        if (originalUrl == null) {
            // 2. Cache miss — query database
            originalUrl = shortLinkService.getOriginalUrlByShortCode(shortCode);

            if (originalUrl == null) {
                log.debug("Short link not found or expired: {}", shortCode);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Short link not found or expired");
                return;
            }

            // 3. Populate cache for subsequent requests (24h TTL)
            redisTemplate.opsForValue().set(cacheKey, originalUrl, 24, TimeUnit.HOURS);
        }

        // 4. Record click asynchronously (non-blocking)
        statsService.recordClickAsync(shortCode, request);

        // 5. Issue 302 redirect
        response.sendRedirect(originalUrl);
    }
}
