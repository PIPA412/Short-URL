package com.shortlink.common.utils;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * User-Agent parsing utility.
 * Extracts browser, OS, and device type from the User-Agent header
 * using Hutool's {@code UserAgentUtil}.
 *
 * @author ShortLink
 */
public class UserAgentUtils {

    /**
     * Parse the User-Agent header from a request.
     *
     * @param request HTTP servlet request
     * @return parsed UserAgent object, or {@code null} if header is missing
     */
    public static UserAgent parse(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ua = request.getHeader("User-Agent");
        return parse(ua);
    }

    /**
     * Parse a User-Agent string.
     *
     * @param userAgent user-agent string
     * @return parsed UserAgent object, or {@code null} if input is blank
     */
    public static UserAgent parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return UserAgentUtil.parse(userAgent);
    }

    /**
     * Determine device type from a User-Agent string.
     *
     * @param request HTTP servlet request
     * @return "PC", "MOBILE", "TABLET", or "UNKNOWN"
     */
    public static String getDeviceType(HttpServletRequest request) {
        UserAgent ua = parse(request);
        if (ua == null) {
            return "UNKNOWN";
        }
        if (ua.isMobile()) {
            return "MOBILE";
        }
        // Hutool doesn't have a direct tablet check; we classify as PC
        return "PC";
    }

    /**
     * Extract browser name from a User-Agent string.
     *
     * @param request HTTP servlet request
     * @return browser name, or "Unknown" if unparseable
     */
    public static String getBrowser(HttpServletRequest request) {
        UserAgent ua = parse(request);
        if (ua == null) {
            return "Unknown";
        }
        String browser = ua.getBrowser() != null ? ua.getBrowser().toString() : null;
        return browser != null && !browser.isEmpty() ? browser : "Unknown";
    }

    /**
     * Extract OS name from a User-Agent string.
     *
     * @param request HTTP servlet request
     * @return OS name, or "Unknown" if unparseable
     */
    public static String getOs(HttpServletRequest request) {
        UserAgent ua = parse(request);
        if (ua == null) {
            return "Unknown";
        }
        String os = ua.getOs() != null ? ua.getOs().toString() : null;
        return os != null && !os.isEmpty() ? os : "Unknown";
    }

    private UserAgentUtils() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }
}
