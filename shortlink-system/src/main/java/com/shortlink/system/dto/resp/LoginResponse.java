package com.shortlink.system.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * Login response DTO.
 *
 * @author ShortLink
 */
@Data
@Builder
public class LoginResponse {

    /** JWT token (Bearer) */
    private String token;

    /** User info */
    private UserInfoResponse user;
}
