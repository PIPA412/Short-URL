package com.shortlink.common.exception;

import com.shortlink.common.constant.HttpStatus;

/**
 * Exception for 403 Forbidden errors (insufficient permissions).
 *
 * @author ShortLink
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String msg) {
        super(HttpStatus.FORBIDDEN, msg);
    }
}
