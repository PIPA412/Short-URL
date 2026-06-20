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
 * 点击日志表
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlClickLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 短链接ID
     */
    private Long shortLinkId;

    /**
     * 点击时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date clickTime;

    /**
     * 访问IP
     */
    private String ip;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 来源地址（Referer）
     */
    private String referer;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 浏览器
     */
    private String browser;

}
