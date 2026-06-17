package com.zsc.module.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <p>
 * 短链接表
 * </p>
 *
 * @author author
 * @since 2026-06-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlShortLink {

    /**
     * 链接ID
     */
    @TableId(value = "link_id", type = IdType.AUTO)
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
