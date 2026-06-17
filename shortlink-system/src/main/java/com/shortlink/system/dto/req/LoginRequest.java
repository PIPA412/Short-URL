package com.shortlink.system.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request DTO.
 *
 * @author ShortLink
 */
@Data
public class LoginRequest {

    /** Username */
    @NotBlank(message = "Username is required")
    private String username;

    /** Plain-text password */
    @NotBlank(message = "Password is required")
    private String password;
}
