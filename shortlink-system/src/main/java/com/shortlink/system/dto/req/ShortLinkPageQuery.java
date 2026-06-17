package com.shortlink.system.dto.req;

import com.shortlink.common.dto.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Short link paginated query DTO.
 *
 * @author ShortLink
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShortLinkPageQuery extends BasePageQuery {

    /** Filter by title (fuzzy match) */
    private String title;

    /** Filter by status: 0=NORMAL, 1=EXPIRED, 2=DISABLED */
    private Integer status;
}
