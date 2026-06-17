package com.shortlink.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.common.core.PageResult;
import com.shortlink.common.core.R;
import com.shortlink.system.dto.req.AccessLogPageQuery;
import com.shortlink.system.dto.resp.AccessLogResponse;
import com.shortlink.system.dto.resp.StatsResponse;
import com.shortlink.system.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Click statistics controller.
 * <p>
 * Provides analytics endpoints for individual short links:
 * <ul>
 *   <li>{@code GET /api/stats/{linkId}} — full stats</li>
 *   <li>{@code GET /api/stats/{linkId}/chart} — chart-only (ECharts format)</li>
 *   <li>{@code GET /api/stats/{linkId}/logs} — paginated access logs</li>
 * </ul>
 *
 * @author ShortLink
 */
@Tag(name = "Statistics", description = "Click analytics, charts, and access logs")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    // ==================== Full Stats ====================

    @Operation(summary = "Get link stats",
            description = "Comprehensive click statistics: summary numbers, "
                    + "7-day trend, device/browser/referer distributions")
    @GetMapping("/{linkId}")
    public R<StatsResponse> getStats(@PathVariable Long linkId) {
        return R.success(statsService.getStats(linkId));
    }

    // ==================== Chart-Only Data ====================

    @Operation(summary = "Get chart data",
            description = "Daily click counts for the last N days in ECharts format. "
                    + "Returns { dates: [...], counts: [...] } structure.")
    @GetMapping("/{linkId}/chart")
    public R<StatsResponse> getChartData(
            @PathVariable Long linkId,
            @Parameter(description = "Number of days (1–90, default 7)")
            @RequestParam(defaultValue = "7") Integer days) {
        return R.success(statsService.getChartData(linkId, days));
    }

    // ==================== Paginated Access Logs ====================

    @Operation(summary = "Get access logs",
            description = "Paginated list of individual click records with optional "
                    + "time-range filter (startDate / endDate in yyyy-MM-dd HH:mm:ss)")
    @GetMapping("/{linkId}/logs")
    public R<PageResult<AccessLogResponse>> getAccessLogs(
            @PathVariable Long linkId,
            @Valid AccessLogPageQuery query) {
        IPage<AccessLogResponse> page = statsService.getAccessLogs(linkId, query);
        return R.success(PageResult.build(page.getTotal(), page.getRecords()));
    }
}
