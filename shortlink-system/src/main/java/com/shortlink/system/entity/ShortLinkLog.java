package com.shortlink.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Short link access log entity — maps to {@code short_link_log} table.
 * <p>
 * Each record represents one click/access event on a short link.
 * This table grows rapidly — consider partitioning by month in production.
 *
 * @author ShortLink
 */
@Data
@TableName("short_link_log")
public class ShortLinkLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key (auto-increment) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Short link ID — foreign key to {@code short_link.id} */
    private Long linkId;

    /** Short code (redundant — enables fast queries without join) */
    private String shortCode;

    /** Visitor IP address */
    private String accessIp;

    /** Raw User-Agent header */
    private String userAgent;

    /** HTTP Referer header */
    private String referer;

    /** Geolocation: country */
    private String country;

    /** Geolocation: city */
    private String city;

    /** Device type: PC, MOBILE, TABLET */
    private String deviceType;

    /** Browser name (parsed from User-Agent) */
    private String browser;

    /** Operating system (parsed from User-Agent) */
    private String os;

    /** Access timestamp */
    private LocalDateTime accessTime;

    /** Record creation timestamp */
    private LocalDateTime createTime;
}
