package com.shortlink.system.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * Short link detail response DTO.
 *
 * @author ShortLink
 */
@Data
@Builder
public class ShortLinkResponse {

    private Long id;
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private String title;
    private String description;
    private String expireTime;
    private Long clickCount;
    private Integer status;
    private String statusDesc;
    private String createTime;
    private String updateTime;
}
