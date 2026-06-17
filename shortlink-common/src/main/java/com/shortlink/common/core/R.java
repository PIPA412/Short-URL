package com.shortlink.common.core;

import com.shortlink.common.constant.HttpStatus;

import java.util.HashMap;

/**
 * Unified API response wrapper.
 * <p>
 * All REST controllers return {@code R<T>} to ensure a consistent
 * response format: {@code {"code": 200, "msg": "success", "data": ...}}.
 * <p>
 * Usage:
 * <pre>{@code
 *   return R.success(data);          // 200 with payload
 *   return R.error("Invalid input"); // 500 with message
 *   return R.error(400, "Bad");      // custom code with message
 * }</pre>
 *
 * @param <T> type of the data payload
 * @author ShortLink
 */
public class R<T> extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    /** Key constant for the "code" field */
    private static final String CODE_KEY = "code";

    /** Key constant for the "msg" field */
    private static final String MSG_KEY = "msg";

    /** Key constant for the "data" field */
    private static final String DATA_KEY = "data";

    public R() {
        put(CODE_KEY, HttpStatus.SUCCESS);
        put(MSG_KEY, "success");
    }

    // ==================== Static Factory Methods ====================

    /**
     * Success response without data.
     */
    public static <T> R<T> success() {
        return new R<>();
    }

    /**
     * Success response with data payload.
     *
     * @param data the response data
     */
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        if (data != null) {
            r.put(DATA_KEY, data);
        }
        return r;
    }

    /**
     * Success response with data and custom message.
     */
    public static <T> R<T> success(T data, String msg) {
        R<T> r = success(data);
        r.put(MSG_KEY, msg);
        return r;
    }

    /**
     * Error response with default code (500).
     */
    public static <T> R<T> error(String msg) {
        return error(HttpStatus.ERROR, msg);
    }

    /**
     * Error response with custom HTTP status code.
     */
    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.put(CODE_KEY, code);
        r.put(MSG_KEY, msg);
        return r;
    }

    // ==================== Fluent Setters ====================

    @Override
    @SuppressWarnings("unchecked")
    public R<T> put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    // ==================== Convenience Getters ====================

    public int getCode() {
        Object code = get(CODE_KEY);
        return code instanceof Integer ? (Integer) code : HttpStatus.SUCCESS;
    }

    public String getMsg() {
        Object msg = get(MSG_KEY);
        return msg != null ? msg.toString() : "success";
    }

    @SuppressWarnings("unchecked")
    public T getData() {
        return (T) get(DATA_KEY);
    }
}
