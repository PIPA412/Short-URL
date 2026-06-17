package com.shortlink.system.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Short link update request DTO.
 *
 * @author ShortLink
 */
@Data
public class ShortLinkUpdateRequest {

    /** Link ID (required) */
    @NotNull(message = "Link ID is required")
    private Long id;

    /** Updated original URL */
    @Size(max = 2048, message = "URL must be at most 2048 characters")
    private String originalUrl;

    /** Updated title */
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    /** Updated description */
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    /** Updated expiration time */
    private LocalDateTime expireTime;

    /** Updated status: 0=NORMAL, 1=EXPIRED, 2=DISABLED */
    private Integer status;
}
