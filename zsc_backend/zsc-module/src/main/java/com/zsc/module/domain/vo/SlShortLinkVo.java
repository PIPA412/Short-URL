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
     * 链接ID
     */
    private Long linkId;

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 点击次数
     */
    private Long clicks;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

}
