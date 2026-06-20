package com.zsc.module.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <p>
 * 黑名单域名表
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlBlacklistDomain {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 域名（如 example.com）
     */
    private String domain;

    /**
     * 创建时间
     */
    private Date createTime;

}
