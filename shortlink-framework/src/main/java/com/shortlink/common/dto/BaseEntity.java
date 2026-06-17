package com.shortlink.common.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity with common auto-fill fields.
 * <p>
 * All domain entities extend this class to inherit:
 * <ul>
 *   <li>createTime — auto-filled on insert via {@link com.baomidou.mybatisplus.core.handlers.MetaObjectHandler}</li>
 *   <li>updateTime — auto-filled on insert and update</li>
 *   <li>createBy — filled by the current user (reserved for v2 RBAC)</li>
 *   <li>updateBy — filled by the current user (reserved for v2 RBAC)</li>
 * </ul>
 * <p>
 * Located in shortlink-framework (not common) because it depends on
 * MyBatis-Plus annotations ({@code @TableField}, {@code FieldFill}).
 *
 * @author ShortLink
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Creation time — auto-filled on insert */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** Last update time — auto-filled on insert and update */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** Creator username (reserved for v2 RBAC — no DB column yet) */
    @TableField(exist = false)
    private String createBy;

    /** Updater username (reserved for v2 RBAC — no DB column yet) */
    @TableField(exist = false)
    private String updateBy;
}
