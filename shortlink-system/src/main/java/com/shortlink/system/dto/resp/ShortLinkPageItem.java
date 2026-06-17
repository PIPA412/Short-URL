package com.shortlink.system.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * Short link list item (summary) response DTO.
 *
 * @author ShortLink
 */
@Data
@Builder
public class ShortLinkPageItem {

    private Long id;
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private String title;
    private String expireTime;
    private Long clickCount;
    private Integer status;
    private String statusDesc;
    private String createTime;
}
