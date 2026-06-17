package com.shortlink.common.exception;

import com.shortlink.common.constant.HttpStatus;

/**
 * Exception for 404 Not Found errors (resource does not exist).
 *
 * @author ShortLink
 */
public class NotFoundException extends BaseException {

    public NotFoundException(String msg) {
        super(HttpStatus.NOT_FOUND, msg);
    }
}
