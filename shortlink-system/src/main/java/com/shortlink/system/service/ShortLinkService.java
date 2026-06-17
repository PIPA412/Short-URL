package com.shortlink.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.system.dto.req.ShortLinkCreateRequest;
import com.shortlink.system.dto.req.ShortLinkUpdateRequest;
import com.shortlink.system.dto.req.ShortLinkPageQuery;
import com.shortlink.system.dto.resp.ShortLinkResponse;
import com.shortlink.system.dto.resp.ShortLinkPageItem;
import com.shortlink.system.entity.ShortLink;

/**
 * Short link service interface.
 *
 * @author ShortLink
 */
public interface ShortLinkService {

    /**
     * Create a new short link.
     *
     * @param userId  creator user ID
     * @param request creation request
     * @return the created short link response
     */
    ShortLinkResponse create(Long userId, ShortLinkCreateRequest request);

    /**
     * Update an existing short link.
     *
     * @param userId  current user ID (for ownership check)
     * @param request update request
     */
    void update(Long userId, ShortLinkUpdateRequest request);

    /**
     * Delete a short link (logical delete).
     *
     * @param userId current user ID (for ownership check)
     * @param linkId link ID to delete
     */
    void delete(Long userId, Long linkId);

    /**
     * Get short link detail by ID.
     *
     * @param linkId link ID
     * @return short link response
     */
    ShortLinkResponse getById(Long linkId);

    /**
     * Get short link entity by ID.
     */
    ShortLink getEntityById(Long linkId);

    /**
     * Paginated query of short links.
     *
     * @param userId current user ID
     * @param query  pagination and filter parameters
     * @return page of short link list items
     */
    IPage<ShortLinkPageItem> pageQuery(Long userId, ShortLinkPageQuery query);

    /**
     * Get the original URL by short code (for redirect).
     * Uses cache-aside pattern: Redis first, DB fallback.
     *
     * @param shortCode the short code
     * @return original URL, or null if not found
     */
    String getOriginalUrlByShortCode(String shortCode);

    /**
     * Get short link entity by short code.
     */
    ShortLink getByShortCode(String shortCode);
}
