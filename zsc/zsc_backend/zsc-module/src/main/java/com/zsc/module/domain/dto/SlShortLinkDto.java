package com.zsc.module.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlShortLinkDto {

    /**
     * 链接ID（更新时必填）
     */
    private Long id;

    /**
     * 短码（可选，为空则自动生成）
     */
    private String shortCode;

    /**
     * 原始URL
     */
    @NotBlank(message = "原始URL不能为空")
    private String originalUrl;

    /**
     * 状态（0-正常 1-过期 2-禁用 3-异常，为空时默认 0）
     */
    private String status;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

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

    /**
     * 图形验证码（同一 IP 连续生成超过 10 次后需提供）
     */
    private String captchaCode;

    /**
     * 图形验证码唯一标识
     */
    private String captchaUuid;

}
