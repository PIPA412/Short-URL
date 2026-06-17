package com.shortlink.system.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Short link creation request DTO.
 *
 * @author ShortLink
 */
@Data
public class ShortLinkCreateRequest {

    /** Original long URL (required, max 2048 characters) */
    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL must be at most 2048 characters")
    private String originalUrl;

    /** Link title (optional) */
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    /** Link description (optional) */
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    /** Expiration time (optional, NULL = never expires) */
    private LocalDateTime expireTime;
}
