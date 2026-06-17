package com.shortlink.system.security;

import com.shortlink.common.enums.UserStatusEnum;
import com.shortlink.system.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security {@link UserDetails} implementation wrapping {@link SysUser}.
 * <p>
 * Maps the application's user entity into the Spring Security authentication
 * domain. In v1, all authenticated users have the same empty authority set
 * (no RBAC). Roles can be added in a future version.
 * <p>
 * Located in the system module because it depends on {@link SysUser}.
 *
 * @author ShortLink
 */
@Getter
public class ShortLinkUserDetails implements UserDetails {

    private final SysUser user;

    public ShortLinkUserDetails(SysUser user) {
        this.user = user;
    }

    /**
     * Get the underlying SysUser entity.
     */
    public SysUser getSysUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // v1: no roles — all authenticated users have the same access level
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatusEnum.DISABLED.getCode();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatusEnum.OK.getCode();
    }
}
