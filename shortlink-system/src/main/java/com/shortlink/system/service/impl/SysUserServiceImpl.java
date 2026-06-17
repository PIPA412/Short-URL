package com.shortlink.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.common.enums.UserStatusEnum;
import com.shortlink.common.exception.BadRequestException;
import com.shortlink.common.exception.UnauthorizedException;
import com.shortlink.common.utils.PasswordEncoder;
import com.shortlink.system.dto.req.RegisterRequest;
import com.shortlink.system.dto.resp.UserInfoResponse;
import com.shortlink.system.entity.SysUser;
import com.shortlink.system.mapper.SysUserMapper;
import com.shortlink.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User service implementation.
 * <p>
 * Password verification during login is handled by Spring Security's
 * {@code DaoAuthenticationProvider} — see
 * {@link com.shortlink.system.controller.AuthController#login}.
 *
 * @author ShortLink
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    // ==================== Registration ====================

    @Override
    @Transactional
    public SysUser register(RegisterRequest request) {
        // Check username uniqueness
        SysUser existing = findByUsername(request.getUsername());
        if (existing != null) {
            throw new BadRequestException(
                    "Username already exists: " + request.getUsername());
        }

        // Create user with BCrypt-hashed password
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null
                ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(UserStatusEnum.OK.getCode());
        user.setCreateTime(LocalDateTime.now());

        sysUserMapper.insert(user);

        log.info("New user registered: id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    // ==================== Queries ====================

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        SysUser user = findById(userId);
        if (user == null) {
            throw new UnauthorizedException("User not found: id=" + userId);
        }

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createTime(formatDateTime(user.getCreateTime()))
                .build();
    }

    @Override
    public SysUser findByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
    }

    @Override
    public SysUser findById(Long userId) {
        return sysUserMapper.selectById(userId);
    }

    // ==================== Updates ====================

    @Override
    public void updateLoginInfo(Long userId, String loginIp) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLoginIp(loginIp);
        user.setLoginDate(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    // ==================== Private Helpers ====================

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
