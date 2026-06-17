package com.shortlink.framework.handler;

import com.shortlink.common.constant.HttpStatus;
import com.shortlink.common.core.R;
import com.shortlink.common.exception.BadRequestException;
import com.shortlink.common.exception.BaseException;
import com.shortlink.common.exception.ForbiddenException;
import com.shortlink.common.exception.NotFoundException;
import com.shortlink.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Global exception handler.
 * <p>
 * Intercepts all exceptions thrown by controllers and translates them
 * into the unified {@link R} response format.
 * <p>
 * Each handler method maps a specific exception type to an appropriate
 * HTTP status code and error message.
 *
 * @author ShortLink
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== Custom Exceptions ====================

    @ExceptionHandler(BadRequestException.class)
    public R<Void> handleBadRequest(BadRequestException e) {
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public R<Void> handleUnauthorized(UnauthorizedException e) {
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(ForbiddenException.class)
    public R<Void> handleForbidden(ForbiddenException e) {
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(NotFoundException.class)
    public R<Void> handleNotFound(NotFoundException e) {
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(BaseException.class)
    public R<Void> handleBaseException(BaseException e) {
        return R.error(e.getCode(), e.getMsg());
    }

    // ==================== Spring Security ====================

    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDenied(AccessDeniedException e) {
        return R.error(HttpStatus.FORBIDDEN, "Access denied: insufficient permissions");
    }

    /**
     * Safety net for any Spring Security {@link AuthenticationException}
     * that escapes the controller layer (e.g., bad credentials, disabled account).
     */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return R.error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // ==================== Validation Errors ====================

    /**
     * Handles {@code @Valid} / {@code @Validated} failures on request body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return R.error(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles {@code @Valid} failures on query parameters / form data.
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.error(HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.error(HttpStatus.BAD_REQUEST,
                "Parameter type mismatch: " + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return R.error(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    // ==================== HTTP Method ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.error(HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed. Supported: " + e.getSupportedHttpMethods());
    }

    // ==================== Database ====================

    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKey(DuplicateKeyException e, HttpServletRequest request) {
        log.warn("Duplicate key violation: {} | URL: {}", e.getMostSpecificCause().getMessage(),
                request.getRequestURI());
        return R.error(HttpStatus.CONFLICT, "Resource already exists");
    }

    // ==================== Catch-All ====================

    /**
     * Last-resort handler for any unhandled exception.
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception at [{}] {}: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage(), e);
        return R.error(HttpStatus.ERROR, "Internal server error");
    }
}
