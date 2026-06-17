package com.shortlink.system.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Comprehensive click statistics response DTO.
 *
 * @author ShortLink
 */
@Data
@Builder
public class StatsResponse {

    // ==================== Summary ====================

    /** Total click count (all time) */
    private Long totalClicks;

    /** Number of unique IPs that accessed this link */
    private Long uniqueIps;

    /** Click count for today */
    private Long todayClicks;

    /** ISO datetime of the most recent access */
    private String lastAccessTime;

    // ==================== Trend Chart ====================

    /** Daily click counts for trend chart: [{date, count}] */
    private List<ChartPoint> chartData;

    // ==================== Distributions ====================

    /** Device type distribution: [{name: "PC", value: 150}, ...] */
    private List<NameValue> deviceDistribution;

    /** Browser distribution: [{name: "Chrome", value: 80}, ...] */
    private List<NameValue> browserDistribution;

    /** Referer TOP 10: [{name: "https://...", value: 50}, ...] */
    private List<NameValue> refererTop10;

    // ==================== Inner Types ====================

    /**
     * A single data point for ECharts trend charts.
     */
    @Data
    @Builder
    public static class ChartPoint {
        private String date;
        private Long count;
    }

    /**
     * A name-value pair for pie charts and bar charts.
     * Compatible with ECharts dataset format.
     */
    @Data
    @Builder
    public static class NameValue {
        private String name;
        private Long value;
    }
}
