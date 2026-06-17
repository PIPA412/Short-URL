package com.shortlink.system.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * User registration request DTO.
 *
 * @author ShortLink
 */
@Data
public class RegisterRequest {

    /** Username (3–50 characters) */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    private String username;

    /** Plain-text password (6–100 characters) */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be 6–100 characters")
    private String password;

    /** Display nickname (optional) */
    @Size(max = 50, message = "Nickname must be at most 50 characters")
    private String nickname;

    /** Email address (optional but validated if provided) */
    @Email(message = "Invalid email format")
    private String email;
}
