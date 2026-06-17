package com.shortlink.framework.filter;

import com.shortlink.common.utils.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Request logging filter.
 * <p>
 * Logs every incoming request with method, URI, IP, and elapsed time.
 * Runs early in the filter chain so it captures the total processing time
 * including security and other filters.
 *
 * @author ShortLink
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String ip = IpUtils.getIpAddr(request);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            String fullUri = query != null ? uri + "?" + query : uri;

            if (status >= 500) {
                log.error("[REQ] {} {} | IP: {} | Status: {} | {}ms",
                        method, fullUri, ip, status, elapsed);
            } else if (status >= 400) {
                log.warn("[REQ] {} {} | IP: {} | Status: {} | {}ms",
                        method, fullUri, ip, status, elapsed);
            } else {
                log.info("[REQ] {} {} | IP: {} | Status: {} | {}ms",
                        method, fullUri, ip, status, elapsed);
            }
        }
    }
}
