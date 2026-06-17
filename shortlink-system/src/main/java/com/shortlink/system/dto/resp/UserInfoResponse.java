package com.shortlink.system.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * User info response DTO.
 *
 * @author ShortLink
 */
@Data
@Builder
public class UserInfoResponse {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String createTime;
}
