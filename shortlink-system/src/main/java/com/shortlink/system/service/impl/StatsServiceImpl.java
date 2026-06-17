package com.shortlink.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.exception.NotFoundException;
import com.shortlink.common.utils.IpUtils;
import com.shortlink.common.utils.UserAgentUtils;
import com.shortlink.system.dto.req.AccessLogPageQuery;
import com.shortlink.system.dto.resp.AccessLogResponse;
import com.shortlink.system.dto.resp.StatsResponse;
import com.shortlink.system.entity.ShortLink;
import com.shortlink.system.entity.ShortLinkLog;
import com.shortlink.system.mapper.ShortLinkLogMapper;
import com.shortlink.system.mapper.ShortLinkMapper;
import com.shortlink.system.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Click statistics service implementation.
 * <p>
 * Aggregates click data from {@code short_link_log} and produces
 * summary statistics, chart data, and distribution breakdowns.
 *
 * @author ShortLink
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkLogMapper shortLinkLogMapper;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== Comprehensive Stats ====================

    @Override
    public StatsResponse getStats(Long linkId) {
        ensureLinkExists(linkId);

        // --- Summary ---
        String todayStart = LocalDate.now().atStartOfDay().format(DT_FMT);
        String todayEnd   = LocalDate.now().plusDays(1).atStartOfDay().format(DT_FMT);

        Long totalClicks = shortLinkLogMapper.countByLinkIdAndDateRange(
                linkId, "2000-01-01 00:00:00", "2099-12-31 23:59:59");
        Long todayClicks = shortLinkLogMapper.countByLinkIdAndDateRange(
                linkId, todayStart, todayEnd);
        Long uniqueIps = shortLinkLogMapper.countUniqueIps(linkId);
        String lastAccess = shortLinkLogMapper.getLastAccessTime(linkId);

        // --- Trend Chart (7 days) ---
        List<StatsResponse.ChartPoint> chartData = buildChartData(linkId, 7);

        // --- Distributions ---
        List<StatsResponse.NameValue> deviceDist = mapNameValue(
                shortLinkLogMapper.countByDeviceType(linkId));
        List<StatsResponse.NameValue> browserDist = mapNameValue(
                shortLinkLogMapper.countByBrowser(linkId));
        List<StatsResponse.NameValue> refererTop10 = mapNameValue(
                shortLinkLogMapper.countByReferer(linkId));

        return StatsResponse.builder()
                .totalClicks(nullToZero(totalClicks))
                .uniqueIps(nullToZero(uniqueIps))
                .todayClicks(nullToZero(todayClicks))
                .lastAccessTime(lastAccess)
                .chartData(chartData)
                .deviceDistribution(deviceDist)
                .browserDistribution(browserDist)
                .refererTop10(refererTop10)
                .build();
    }

    // ==================== Chart-Only Data ====================

    @Override
    public StatsResponse getChartData(Long linkId, Integer days) {
        ensureLinkExists(linkId);
        int d = days != null && days > 0 && days <= 90 ? days : 7;
        List<StatsResponse.ChartPoint> chartData = buildChartData(linkId, d);
        return StatsResponse.builder()
                .chartData(chartData)
                .build();
    }

    // ==================== Paginated Access Logs ====================

    @Override
    public IPage<AccessLogResponse> getAccessLogs(Long linkId, AccessLogPageQuery query) {
        ensureLinkExists(linkId);

        Page<ShortLinkLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ShortLinkLog> logPage = shortLinkLogMapper.selectPageByLinkId(
                page, linkId, query.getStartDate(), query.getEndDate());

        List<AccessLogResponse> items = logPage.getRecords().stream()
                .map(this::toLogResponse)
                .collect(Collectors.toList());

        Page<AccessLogResponse> respPage =
                new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        respPage.setRecords(items);
        return respPage;
    }

    // ==================== Async Click Recording ====================

    @Override
    @Async
    public void recordClickAsync(String shortCode, HttpServletRequest request) {
        try {
            ShortLink link = shortLinkMapper.selectOne(
                    new LambdaQueryWrapper<ShortLink>()
                            .eq(ShortLink::getShortCode, shortCode));
            if (link == null) return;

            ShortLinkLog logEntry = new ShortLinkLog();
            logEntry.setLinkId(link.getId());
            logEntry.setShortCode(shortCode);
            logEntry.setAccessIp(IpUtils.getIpAddr(request));
            logEntry.setUserAgent(request.getHeader("User-Agent"));
            logEntry.setReferer(request.getHeader("Referer"));
            logEntry.setDeviceType(UserAgentUtils.getDeviceType(request));
            logEntry.setBrowser(UserAgentUtils.getBrowser(request));
            logEntry.setOs(UserAgentUtils.getOs(request));
            logEntry.setAccessTime(LocalDateTime.now());
            logEntry.setCreateTime(LocalDateTime.now());

            shortLinkLogMapper.insert(logEntry);

            // Atomic click count increment
            ShortLink update = new ShortLink();
            update.setId(link.getId());
            update.setClickCount(
                    (link.getClickCount() != null ? link.getClickCount() : 0) + 1);
            shortLinkMapper.updateById(update);

        } catch (Exception e) {
            log.error("Failed to record click for shortCode={}: {}",
                    shortCode, e.getMessage(), e);
        }
    }

    // ==================== Private Helpers ====================

    private void ensureLinkExists(Long linkId) {
        if (shortLinkMapper.selectById(linkId) == null) {
            throw new NotFoundException("Short link not found: id=" + linkId);
        }
    }

    /**
     * Build chart data with zero-fill for missing days.
     */
    private List<StatsResponse.ChartPoint> buildChartData(Long linkId, int days) {
        List<Map<String, Object>> rows =
                shortLinkLogMapper.countDailyByLinkId(linkId, days);

        List<StatsResponse.ChartPoint> raw = rows.stream()
                .map(m -> StatsResponse.ChartPoint.builder()
                        .date(String.valueOf(m.get("date")))
                        .count(Long.valueOf(String.valueOf(m.get("count"))))
                        .build())
                .collect(Collectors.toList());

        return fillMissingDays(raw, days);
    }

    /**
     * Fill in missing dates with zero counts to produce a continuous time series.
     */
    private List<StatsResponse.ChartPoint> fillMissingDays(
            List<StatsResponse.ChartPoint> data, int days) {
        List<StatsResponse.ChartPoint> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            StatsResponse.ChartPoint point = data.stream()
                    .filter(p -> date.equals(p.getDate()))
                    .findFirst()
                    .orElse(StatsResponse.ChartPoint.builder()
                            .date(date).count(0L).build());
            result.add(point);
        }
        return result;
    }

    /**
     * Map raw MyBatis result rows to NameValue DTOs.
     */
    private List<StatsResponse.NameValue> mapNameValue(List<Map<String, Object>> rows) {
        if (rows == null) return List.of();
        return rows.stream()
                .map(m -> StatsResponse.NameValue.builder()
                        .name(String.valueOf(m.get("name")))
                        .value(Long.valueOf(String.valueOf(m.get("value"))))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map a ShortLinkLog entity to the AccessLogResponse DTO.
     */
    private AccessLogResponse toLogResponse(ShortLinkLog log) {
        return AccessLogResponse.builder()
                .id(log.getId())
                .accessIp(log.getAccessIp())
                .deviceType(log.getDeviceType())
                .browser(log.getBrowser())
                .os(log.getOs())
                .referer(log.getReferer())
                .accessTime(log.getAccessTime() != null
                        ? log.getAccessTime().format(DT_FMT) : null)
                .build();
    }

    private Long nullToZero(Long v) {
        return v != null ? v : 0L;
    }
}
