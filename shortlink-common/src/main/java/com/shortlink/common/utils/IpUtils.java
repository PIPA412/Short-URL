package com.shortlink.common.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * IP address utility.
 * Extracts the real client IP from a request, considering proxy headers.
 *
 * @author ShortLink
 */
public class IpUtils {

    /** Localhost IPv4 */
    private static final String LOCALHOST_V4 = "127.0.0.1";

    /** Localhost IPv6 */
    private static final String LOCALHOST_V6 = "0:0:0:0:0:0:0:1";

    /** Unknown marker value */
    private static final String UNKNOWN = "unknown";

    /** IP v4 segment separator */
    private static final String IPV4_SEPARATOR = ".";

    /**
     * Extract the real client IP address from an HTTP request.
     * <p>
     * Checks proxy headers in order: X-Forwarded-For, X-Real-IP,
     * Proxy-Client-IP, WL-Proxy-Client-IP, HTTP_CLIENT_IP,
     * HTTP_X_FORWARDED_FOR.
     *
     * @param request the HTTP servlet request
     * @return client IP address string
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return LOCALHOST_V4;
        }

        String ip = null;

        // Check common proxy/load-balancer headers
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }

        // Fall back to the remote address from the TCP connection
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multi-proxy case: X-Forwarded-For may contain a comma-separated list
        // The first IP is the original client
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // Normalize IPv6 localhost
        if (LOCALHOST_V6.equals(ip)) {
            ip = LOCALHOST_V4;
        }

        return ip;
    }

    /**
     * Check if an IP string is valid (non-null, non-blank, not "unknown").
     */
    private static boolean isValidIp(String ip) {
        return StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip.trim());
    }

    /**
     * Check if an IP is an internal/local address.
     */
    public static boolean isInternalIp(String ip) {
        if (LOCALHOST_V4.equals(ip) || LOCALHOST_V6.equals(ip)) {
            return true;
        }
        // Check private ranges: 10.x.x.x, 172.16-31.x.x, 192.168.x.x
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            try {
                int secondOctet = Integer.parseInt(
                        ip.substring(IPV4_SEPARATOR.length(),
                                ip.indexOf(IPV4_SEPARATOR, IPV4_SEPARATOR.length() + 1)));
                return secondOctet >= 16 && secondOctet <= 31;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private IpUtils() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }
}
