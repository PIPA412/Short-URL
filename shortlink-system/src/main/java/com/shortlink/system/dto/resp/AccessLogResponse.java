package com.shortlink.system.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * Access log item for the paginated logs endpoint.
 *
 * @author ShortLink
 */
@Data
@Builder
public class AccessLogResponse {

    private Long id;

    /** Visitor IP address */
    private String accessIp;

    /** Device type: PC / MOBILE / TABLET */
    private String deviceType;

    /** Browser name */
    private String browser;

    /** Operating system */
    private String os;

    /** Referring URL */
    private String referer;

    /** Access timestamp */
    private String accessTime;
}
