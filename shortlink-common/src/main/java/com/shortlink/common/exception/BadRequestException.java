package com.shortlink.common.exception;

import com.shortlink.common.constant.HttpStatus;

/**
 * Exception for 400 Bad Request errors (invalid input, validation failure).
 *
 * @author ShortLink
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String msg) {
        super(HttpStatus.BAD_REQUEST, msg);
    }

    public BadRequestException(String msg, Object... args) {
        super(HttpStatus.BAD_REQUEST, msg, args);
    }
}
