package com.shortlink.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.system.entity.ShortLinkLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * ShortLinkLog mapper — extends MyBatis-Plus BaseMapper for standard CRUD,
 * plus custom analytics queries.
 *
 * @author ShortLink
 */
@Mapper
public interface ShortLinkLogMapper extends BaseMapper<ShortLinkLog> {

    // ==================== Aggregate Statistics ====================

    /** Count unique IPs for a given link. */
    Long countUniqueIps(@Param("linkId") Long linkId);

    /** Count clicks within a date range. */
    Long countByLinkIdAndDateRange(@Param("linkId") Long linkId,
                                    @Param("start") String start,
                                    @Param("end") String end);

    /** Daily click counts for chart display. */
    List<Map<String, Object>> countDailyByLinkId(@Param("linkId") Long linkId,
                                                   @Param("days") Integer days);

    /** Device type distribution: [{name:"PC", value:150}, ...] */
    List<Map<String, Object>> countByDeviceType(@Param("linkId") Long linkId);

    /** Browser distribution: [{name:"Chrome", value:80}, ...] */
    List<Map<String, Object>> countByBrowser(@Param("linkId") Long linkId);

    /** Referer TOP 10: [{name:"https://google.com", value:50}, ...] */
    List<Map<String, Object>> countByReferer(@Param("linkId") Long linkId);

    /** Get the most recent access timestamp. */
    String getLastAccessTime(@Param("linkId") Long linkId);

    // ==================== Paginated Logs ====================

    /** Paginated click log with optional time-range filter. */
    IPage<ShortLinkLog> selectPageByLinkId(Page<ShortLinkLog> page,
                                            @Param("linkId") Long linkId,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);
}
