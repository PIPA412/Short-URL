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
     * 链接ID
     */
    private Long linkId;

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
     * 标题
     */
    private String title;

    /**
     * 状态（0正常 1停用）
     */
    @NotBlank(message = "状态不能为空")
    private String status;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 备注
     */
    private String remark;

}
