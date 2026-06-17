package com.shortlink.common.core;

import com.shortlink.common.constant.HttpStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Paginated response wrapper.
 * <p>
 * Standard format for all list endpoints:
 * <pre>{@code
 *   {
 *     "code": 200,
 *     "msg": "success",
 *     "total": 100,
 *     "rows": [...]
 *   }
 * }</pre>
 * <p>
 * Note: this class lives in shortlink-common (no MyBatis-Plus dependency).
 * To build from a MyBatis-Plus {@code IPage}, use
 * {@code PageResult.build(page.getTotal(), page.getRecords())} at the call site.
 *
 * @param <T> type of items in the page
 * @author ShortLink
 */
@Data
@NoArgsConstructor
public class PageResult<T> {

    /** HTTP status code */
    private int code;

    /** Response message */
    private String msg;

    /** Total number of records (across all pages) */
    private long total;

    /** Current page records */
    private List<T> rows;

    /**
     * Build an empty page result.
     */
    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.code = HttpStatus.SUCCESS;
        result.msg = "success";
        result.total = 0;
        result.rows = Collections.emptyList();
        return result;
    }

    /**
     * Build a PageResult from a total count and row list.
     * <p>
     * To build from a MyBatis-Plus {@code IPage}, call:
     * {@code PageResult.build(page.getTotal(), page.getRecords())}
     */
    public static <T> PageResult<T> build(long total, List<T> rows) {
        PageResult<T> result = new PageResult<>();
        result.code = HttpStatus.SUCCESS;
        result.msg = "success";
        result.total = total;
        result.rows = rows != null ? rows : Collections.emptyList();
        return result;
    }
}
