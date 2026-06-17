package com.shortlink.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.common.enums.UserStatusEnum;
import com.shortlink.system.entity.SysUser;
import com.shortlink.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security {@link UserDetailsService} implementation.
 * <p>
 * Supports two lookup modes:
 * <ol>
 *   <li><b>By username</b> — used by {@code DaoAuthenticationProvider} during
 *       {@code AuthenticationManager.authenticate()} in the login flow.
 *       Looks up {@code sys_user} where {@code username} matches exactly.</li>
 *   <li><b>By user ID</b> — used by {@link com.shortlink.framework.security.JwtAuthenticationFilter}
 *       for restoring authentication from a JWT. Falls back to parsing the
 *       input string as a numeric ID.</li>
 * </ol>
 * <p>
 * Account status is validated in both paths:
 * <ul>
 *   <li>User not found → {@link UsernameNotFoundException}</li>
 *   <li>User disabled or deleted → {@link DisabledException}</li>
 * </ul>
 *
 * @author ShortLink
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    /**
     * Load a user by username or user ID.
     * <p>
     * <b>Primary path:</b> Lookup by {@code username} (login flow).
     * <b>Fallback path:</b> If no user is found, parse the input as a
     * numeric user ID and retry (JWT filter flow).
     *
     * @param usernameOrId username (login) or user ID string (JWT restore)
     * @return a populated {@link ShortLinkUserDetails}
     * @throws UsernameNotFoundException if no matching user exists
     * @throws DisabledException         if the account is disabled or deleted
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrId)
            throws UsernameNotFoundException {

        SysUser user = lookupByUsername(usernameOrId);

        // Fallback: try as numeric user ID (used by JwtAuthenticationFilter)
        if (user == null) {
            user = lookupByUserId(usernameOrId);
        }

        // Not found — Spring Security handles this as a bad credential
        if (user == null) {
            log.warn("User not found: identifier={}", usernameOrId);
            throw new UsernameNotFoundException(
                    "User not found with identifier: " + usernameOrId);
        }

        // Account disabled or soft-deleted
        if (user.getStatus() == null || user.getStatus() != UserStatusEnum.OK.getCode()) {
            log.warn("User account is disabled: username={}, status={}",
                    user.getUsername(), user.getStatus());
            throw new DisabledException(
                    "User account is disabled: " + user.getUsername());
        }

        return new ShortLinkUserDetails(user);
    }

    /**
     * Convenience method for direct user-ID lookup (used by
     * {@link com.shortlink.system.controller.AuthController}).
     *
     * @param userId the user's primary key
     * @return a populated {@link ShortLinkUserDetails}
     */
    public ShortLinkUserDetails loadUserById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: id=" + userId);
        }
        if (user.getStatus() == null || user.getStatus() != UserStatusEnum.OK.getCode()) {
            throw new DisabledException("User account is disabled: " + user.getUsername());
        }
        return new ShortLinkUserDetails(user);
    }

    // ==================== Private Helpers ====================

    /**
     * Lookup by exact username match.
     */
    private SysUser lookupByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
    }

    /**
     * Try to parse the input as a numeric user ID and look up.
     * Returns {@code null} if the input is not a valid number or no record exists.
     */
    private SysUser lookupByUserId(String identifier) {
        try {
            long userId = Long.parseLong(identifier);
            return sysUserMapper.selectById(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
