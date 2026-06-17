package com.shortlink.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.shortlink.common.dto.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * System user entity — maps to {@code sys_user} table.
 *
 * @author ShortLink
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** Primary key (auto-increment) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Login username (unique) */
    private String username;

    /** BCrypt hashed password */
    private String password;

    /** Display nickname */
    private String nickname;

    /** Email address */
    private String email;

    /** Avatar URL */
    private String avatar;

    /** Account status: 0=OK, 1=DISABLED */
    private Integer status;

    /** Last login IP address */
    private String loginIp;

    /** Last login datetime */
    private LocalDateTime loginDate;

    /** Logical delete flag: 0=normal, 1=deleted */
    @TableLogic
    private Integer delFlag;
}
