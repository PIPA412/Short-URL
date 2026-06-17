package com.shortlink.framework.interceptor;

import com.shortlink.common.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * Rate limiting interceptor.
 * <p>
 * Limits the number of requests per IP within a time window using
 * a Redis-backed sliding window counter. When the limit is exceeded,
 * the request is rejected with HTTP 429 (Too Many Requests).
 * <p>
 * This interceptor is <strong>optional</strong> and intended for
 * protecting the public redirect endpoint from abuse. It is not
 * registered by default — add it in {@code WebMvcConfig} if needed.
 *
 * @author ShortLink
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    /** Maximum requests per window */
    private static final int MAX_REQUESTS = 100;

    /** Time window in seconds */
    private static final int WINDOW_SECONDS = 60;

    public RateLimitInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String ip = IpUtils.getIpAddr(request);
        String key = "rate:limit:" + ip;

        Long count = redisTemplate.opsForValue().increment(key);

        // Set TTL on first request in the window
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        if (count != null && count > MAX_REQUESTS) {
            log.warn("Rate limit exceeded for IP: {} ({} requests in {}s)", ip, count, WINDOW_SECONDS);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"msg\":\"Too many requests. Please try again later.\"}");
            return false;
        }

        return true;
    }
}
