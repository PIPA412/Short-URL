package com.shortlink.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.shortlink.common.dto.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Short link entity — maps to {@code short_link} table.
 *
 * @author ShortLink
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("short_link")
public class ShortLink extends BaseEntity {

    /** Primary key (auto-increment) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Owner user ID — foreign key to {@code sys_user.id} */
    private Long userId;

    /** Unique short code (Base62 encoded, 7 chars) */
    private String shortCode;

    /** Original long URL (max 2048 characters) */
    private String originalUrl;

    /** Link title (optional) */
    private String title;

    /** Link description (optional) */
    private String description;

    /** Expiration time (NULL = never expires) */
    private LocalDateTime expireTime;

    /** Accumulated click count */
    private Long clickCount;

    /** Link status: 0=NORMAL, 1=EXPIRED, 2=DISABLED */
    private Integer status;

    /** Logical delete flag: 0=normal, 1=deleted */
    @TableLogic
    private Integer delFlag;
}
