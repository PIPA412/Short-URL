package com.shortlink.system.service;

import com.shortlink.system.dto.req.RegisterRequest;
import com.shortlink.system.dto.resp.UserInfoResponse;
import com.shortlink.system.entity.SysUser;

/**
 * User service interface.
 *
 * @author ShortLink
 */
public interface SysUserService {

    /**
     * User registration — creates a new account.
     *
     * @param request registration request
     * @return the created user
     */
    SysUser register(RegisterRequest request);

    /**
     * Get current user info by user ID.
     *
     * @param userId user ID
     * @return user info response
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * Find user by username.
     *
     * @param username username
     * @return user entity, or null if not found
     */
    SysUser findByUsername(String username);

    /**
     * Find user by ID.
     *
     * @param userId user ID
     * @return user entity, or null if not found
     */
    SysUser findById(Long userId);

    /**
     * Update last login info (IP address and timestamp).
     *
     * @param userId  user ID
     * @param loginIp last login IP address
     */
    void updateLoginInfo(Long userId, String loginIp);
}
