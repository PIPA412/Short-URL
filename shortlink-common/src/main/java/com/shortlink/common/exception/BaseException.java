package com.shortlink.common.exception;

import lombok.Getter;

/**
 * Base runtime exception for the application.
 * All custom exceptions extend this to enable centralized handling
 * in {@code GlobalExceptionHandler}.
 *
 * @author ShortLink
 */
@Getter
public class BaseException extends RuntimeException {

    /** HTTP status code to return in the response */
    private final int code;

    /** Error message for the client */
    private final String msg;

    /** Optional arguments for message formatting */
    private final transient Object[] args;

    public BaseException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
        this.args = null;
    }

    public BaseException(int code, String msg, Object... args) {
        super(msg);
        this.code = code;
        this.msg = msg;
        this.args = args;
    }
}
