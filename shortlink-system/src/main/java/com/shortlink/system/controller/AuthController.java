package com.shortlink.system.controller;

import com.shortlink.common.core.R;
import com.shortlink.common.utils.IpUtils;
import com.shortlink.framework.security.JwtTokenProvider;
import com.shortlink.system.dto.req.LoginRequest;
import com.shortlink.system.dto.req.RegisterRequest;
import com.shortlink.system.dto.resp.LoginResponse;
import com.shortlink.system.dto.resp.UserInfoResponse;
import com.shortlink.system.entity.SysUser;
import com.shortlink.system.security.ShortLinkUserDetails;
import com.shortlink.system.security.ShortLinkUserDetailsService;
import com.shortlink.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 * <p>
 * Handles login, registration, and current-user info retrieval.
 * <p>
 * <b>Login flow:</b>
 * <ol>
 *   <li>Client POSTs {@code { username, password }} to {@code /api/auth/login}</li>
 *   <li>Controller calls {@link AuthenticationManager#authenticate},
 *       which triggers {@link ShortLinkUserDetailsService#loadUserByUsername}
 *       and Spring Security's {@code DaoAuthenticationProvider} for BCrypt
 *       password verification.</li>
 *   <li>On success, a JWT token is generated and returned with user info.</li>
 *   <li>Login IP and timestamp are persisted.</li>
 * </ol>
 * <p>
 * All endpoints under {@code /api/auth/**} are publicly accessible
 * (whitelisted in {@link com.shortlink.framework.security.SecurityConfig}).
 *
 * @author ShortLink
 */
@Slf4j
@Tag(name = "Authentication", description = "Login, register, and user info APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // ==================== LOGIN ====================

    @Operation(summary = "User login",
            description = "Authenticate with username/password via Spring Security "
                    + "AuthenticationManager and receive a JWT token")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {

        // Step 1: Authenticate via Spring Security's AuthenticationManager.
        // This calls ShortLinkUserDetailsService.loadUserByUsername(username)
        // and DaoAuthenticationProvider checks the BCrypt password.
        // Throws BadCredentialsException if password is wrong.
        // Throws DisabledException if account is disabled.
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            log.warn("Login failed: bad credentials for username={}", request.getUsername());
            throw new com.shortlink.common.exception.UnauthorizedException(
                    "Invalid username or password");
        } catch (DisabledException e) {
            log.warn("Login failed: disabled account username={}", request.getUsername());
            throw new com.shortlink.common.exception.UnauthorizedException(
                    "Account is disabled");
        }

        // Step 2: Extract user from the authenticated principal
        ShortLinkUserDetails userDetails =
                (ShortLinkUserDetails) authentication.getPrincipal();
        SysUser user = userDetails.getSysUser();

        // Step 3: Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        // Step 4: Update last login info (IP + time)
        sysUserService.updateLoginInfo(user.getId(), IpUtils.getIpAddr(httpRequest));

        // Step 5: Build response (no extra DB query — user is already loaded)
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createTime(formatDateTime(user.getCreateTime()))
                .build();

        log.info("Login success: userId={}, username={}", user.getId(), user.getUsername());

        return R.success(LoginResponse.builder()
                .token(token)
                .user(userInfo)
                .build());
    }

    // ==================== REGISTER ====================

    @Operation(summary = "User registration",
            description = "Create a new account. Username must be unique.")
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request);
        return R.success();
    }

    // ==================== CURRENT USER ====================

    @Operation(summary = "Get current user info",
            description = "Return the authenticated user's profile from the JWT session")
    @GetMapping("/info")
    public R<UserInfoResponse> info(
            @AuthenticationPrincipal ShortLinkUserDetails userDetails) {
        UserInfoResponse userInfo;
        if (userDetails != null) {
            userInfo = sysUserService.getUserInfo(userDetails.getUserId());
        } else {
            // Should not happen with a properly configured JWT filter
            log.warn("/api/auth/info called without authentication");
            userInfo = UserInfoResponse.builder().build();
        }
        return R.success(userInfo);
    }

    // ==================== Private Helpers ====================

    /**
     * Format LocalDateTime to standard string, or null if input is null.
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
