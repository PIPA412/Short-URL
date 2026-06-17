package com.shortlink.common.constant;

/**
 * HTTP response status codes used in unified response wrapper R.java.
 *
 * @author ShortLink
 */
public class HttpStatus {

    /** Operation successful */
    public static final int SUCCESS = 200;

    /** Bad request / validation error */
    public static final int BAD_REQUEST = 400;

    /** Unauthorized / not logged in / token expired */
    public static final int UNAUTHORIZED = 401;

    /** Forbidden / insufficient permissions */
    public static final int FORBIDDEN = 403;

    /** Resource not found */
    public static final int NOT_FOUND = 404;

    /** Method not allowed */
    public static final int METHOD_NOT_ALLOWED = 405;

    /** Conflict (e.g., duplicate resource) */
    public static final int CONFLICT = 409;

    /** Too many requests (rate limit) */
    public static final int TOO_MANY_REQUESTS = 429;

    /** Internal server error */
    public static final int ERROR = 500;

    private HttpStatus() {
        throw new IllegalStateException("Constant class - do not instantiate");
    }
}
