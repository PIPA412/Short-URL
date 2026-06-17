package com.shortlink.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.system.dto.req.AccessLogPageQuery;
import com.shortlink.system.dto.resp.AccessLogResponse;
import com.shortlink.system.dto.resp.StatsResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Click statistics service interface.
 *
 * @author ShortLink
 */
public interface StatsService {

    /**
     * Get comprehensive statistics for a short link.
     * Includes: summary, trend chart data, device/browser/referer distributions.
     *
     * @param linkId short link ID
     * @return full stats response
     */
    StatsResponse getStats(Long linkId);

    /**
     * Get chart-only data for a short link (ECharts format).
     *
     * @param linkId short link ID
     * @param days   number of days to include (default 7)
     * @return stats response with chartData populated
     */
    StatsResponse getChartData(Long linkId, Integer days);

    /**
     * Get paginated access logs for a short link with optional time-range filter.
     *
     * @param linkId short link ID
     * @param query  pagination and filter parameters
     * @return page of access log items
     */
    IPage<AccessLogResponse> getAccessLogs(Long linkId, AccessLogPageQuery query);

    /**
     * Record a click on a short link (async, non-blocking).
     * <p>
     * Extracts visitor info (IP, User-Agent, Referer) from the request,
     * creates a {@code ShortLinkLog} record, and increments the click counter.
     *
     * @param shortCode the short code that was accessed
     * @param request   the HTTP servlet request
     */
    void recordClickAsync(String shortCode, HttpServletRequest request);
}
