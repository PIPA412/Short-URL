package com.shortlink.system.dto.req;

import com.shortlink.common.dto.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Access log paginated query with optional time-range filter.
 *
 * @author ShortLink
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccessLogPageQuery extends BasePageQuery {

    /** Filter: start date (inclusive), format: yyyy-MM-dd HH:mm:ss */
    private String startDate;

    /** Filter: end date (exclusive), format: yyyy-MM-dd HH:mm:ss */
    private String endDate;
}
