package com.shortlink.common.constant;

/**
 * Global constants for the application.
 *
 * @author ShortLink
 */
public class Constants {

    /** UTF-8 charset name */
    public static final String UTF8 = "UTF-8";

    /** HTTP Authorization header name */
    public static final String TOKEN_HEADER = "Authorization";

    /** JWT token prefix in the Authorization header */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Token type: Login user */
    public static final String LOGIN_USER = "login_user";

    /** Token type: Login user key */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /** Request ID header for distributed tracing */
    public static final String REQUEST_ID = "X-Request-Id";

    /** Default date format pattern */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Short code regex pattern: 4–16 alphanumeric characters */
    public static final String SHORT_CODE_PATTERN = "[a-zA-Z0-9]{4,16}";

    /** Maximum original URL length */
    public static final int MAX_URL_LENGTH = 2048;

    /** Default page size for pagination */
    public static final int DEFAULT_PAGE_SIZE = 10;

    private Constants() {
        throw new IllegalStateException("Constant class - do not instantiate");
    }
}
