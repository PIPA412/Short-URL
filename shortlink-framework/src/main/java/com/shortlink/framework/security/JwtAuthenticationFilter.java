package com.shortlink.framework.security;

import com.shortlink.common.constant.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter.
 * <p>
 * Processes every request:
 * <ol>
 *   <li>Extracts the Bearer token from the {@code Authorization} header</li>
 *   <li>Validates the token signature and expiration via {@link JwtTokenProvider}</li>
 *   <li>Loads the user via {@link UserDetailsService}</li>
 *   <li>Sets a fully authenticated {@link SecurityContext}</li>
 * </ol>
 * <p>
 * If any step fails, the security context is cleared and the request
 * continues unauthenticated — the authorization rules in
 * {@link SecurityConfig} then determine whether to allow or reject it.
 * <p>
 * Runs <strong>before</strong> {@code UsernamePasswordAuthenticationFilter}
 * and is <strong>stateless</strong> — no server-side session is created.
 *
 * @author ShortLink
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            try {
                // Restore user identity from JWT
                Long userId = tokenProvider.getUserIdFromToken(token);
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userId.toString());

                // Build a fully authenticated token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication success: userId={}, uri={}",
                        userId, request.getRequestURI());

            } catch (Exception ex) {
                // Token was valid JWT, but user lookup or account status failed
                log.warn("JWT authentication failed for token subject: {} | uri={} | error={}",
                        extractSubjectSafely(token), request.getRequestURI(), ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else if (StringUtils.hasText(token)) {
            // Token present but invalid (expired or tampered)
            log.debug("Invalid or expired JWT token received for uri={}",
                    request.getRequestURI());
            SecurityContextHolder.clearContext();
        }

        // Always proceed — authorization decision is made by SecurityConfig rules
        filterChain.doFilter(request, response);
    }

    /**
     * Extract the Bearer token from the Authorization header.
     *
     * @param request HTTP request
     * @return raw JWT string, or {@code null} if not present or malformed
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * Safely extract the subject from a token without throwing.
     * Used only for logging purposes.
     */
    private String extractSubjectSafely(String token) {
        try {
            return tokenProvider.getUserIdFromToken(token).toString();
        } catch (Exception e) {
            return "<unparseable>";
        }
    }
}
