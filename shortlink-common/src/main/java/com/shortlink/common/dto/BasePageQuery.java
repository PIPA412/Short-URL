package com.shortlink.common.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * Base pagination query DTO.
 * <p>
 * Controllers accepting paginated list requests should extend this class
 * or use it directly as a method parameter.
 *
 * @author ShortLink
 */
@Data
public class BasePageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Page number (1-based) */
    @Min(value = 1, message = "Page number must be at least 1")
    private int pageNum = 1;

    /** Number of records per page */
    @Min(value = 1, message = "Page size must be at least 1")
    private int pageSize = 10;

    /** Order by clause (e.g. "create_time DESC") — validated at the service layer */
    private String orderBy;
}
