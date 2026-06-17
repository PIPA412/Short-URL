package com.shortlink.common.exception;

import com.shortlink.common.constant.HttpStatus;

/**
 * Exception for 401 Unauthorized errors (missing or invalid token).
 *
 * @author ShortLink
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String msg) {
        super(HttpStatus.UNAUTHORIZED, msg);
    }
}
