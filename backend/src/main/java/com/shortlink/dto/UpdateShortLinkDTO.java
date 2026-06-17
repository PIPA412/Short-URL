package com.shortlink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateShortLinkDTO {

    @NotBlank(message = "原始链接不能为空")
    private String originalUrl;

    private LocalDateTime expireTime;

    private Integer status;
}
