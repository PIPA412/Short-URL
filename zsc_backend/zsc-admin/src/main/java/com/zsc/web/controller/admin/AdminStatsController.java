package com.zsc.web.controller.admin;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.domain.entity.SysUser;
import com.zsc.module.domain.entity.SlShortLink;
import com.zsc.module.service.SlShortLinkService;
import com.zsc.system.service.ISysUserService;

/**
 * 管理员 - 全系统统计
 *
 * @author zsc
 */
@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private SlShortLinkService shortLinkService;

    /**
     * 系统概览统计
     */
    @GetMapping("/dashboard")
    public AjaxResult dashboard(@RequestParam(required = false) String beginTime,
                                @RequestParam(required = false) String endTime)
    {
        Map<String, Object> stats = new HashMap<>();

        // === 概览卡片 ===
        // 总用户数
        stats.put("totalUsers", userService.selectUserList(null, new SysUser()).size());
        // 总短链接数
        stats.put("totalLinks", shortLinkService.count());
        // 总点击量
        QueryWrapper<SlShortLink> clickWrapper = new QueryWrapper<>();
        clickWrapper.select("IFNULL(SUM(clicks), 0) as totalClicks");
        Map<String, Object> clickMap = shortLinkService.getMap(clickWrapper);
        stats.put("totalClicks", clickMap != null ? clickMap.get("totalClicks") : 0);

        // === 每日新增链接趋势（近7天） ===
        List<Map<String, Object>> dailyTrend = getDailyTrend();
        stats.put("dailyTrend", dailyTrend);

        // === 热门短链接 TOP10 ===
        List<Map<String, Object>> topLinks = getTopLinks();
        stats.put("topLinks", topLinks);

        // === 最活跃用户 TOP10 ===
        List<Map<String, Object>> activeUsers = getActiveUsers();
        stats.put("activeUsers", activeUsers);

        return success(stats);
    }

    /**
     * 每日新增链接趋势（近7天）
     */
    private List<Map<String, Object>> getDailyTrend()
    {
        QueryWrapper<SlShortLink> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) as date", "COUNT(*) as count")
               .ge("create_time", getDaysAgo(7))
               .groupBy("DATE(create_time)")
               .orderByAsc("DATE(create_time)");
        return shortLinkService.listMaps(wrapper);
    }

    /**
     * 热门短链接 TOP10
     */
    private List<Map<String, Object>> getTopLinks()
    {
        QueryWrapper<SlShortLink> wrapper = new QueryWrapper<>();
        wrapper.select("short_code", "original_url", "title", "clicks", "create_by")
               .orderByDesc("clicks")
               .last("LIMIT 10");
        return shortLinkService.listMaps(wrapper);
    }

    /**
     * 最活跃用户 TOP10（按创建链接数）
     */
    private List<Map<String, Object>> getActiveUsers()
    {
        QueryWrapper<SlShortLink> wrapper = new QueryWrapper<>();
        wrapper.select("create_by", "COUNT(*) as link_count", "SUM(clicks) as total_clicks")
               .groupBy("create_by")
               .orderByDesc("COUNT(*)")
               .last("LIMIT 10");
        return shortLinkService.listMaps(wrapper);
    }

    private String getDaysAgo(int days)
    {
        java.time.LocalDate d = java.time.LocalDate.now().minusDays(days);
        return d.toString() + " 00:00:00";
    }
}
