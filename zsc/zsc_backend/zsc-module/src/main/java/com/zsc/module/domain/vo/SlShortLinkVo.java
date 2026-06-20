package com.zsc.module.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlShortLinkVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 状态（0-正常 1-过期 2-禁用 3-异常）
     */
    private String status;

    /**
     * 健康状态（0-健康 1-异常 2-未知）
     */
    private String healthStatus;

    /**
     * 访问密码（可空）
     */
    private String accessPassword;

    /**
     * 最大点击次数（可空，null表示无限制）
     */
    private Long maxClicks;

}
