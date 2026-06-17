package com.shortlink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkVO {

    private Long id;
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private Long userId;
    private Integer clickCount;
    private LocalDateTime expireTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
