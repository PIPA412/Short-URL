package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsc.common.core.redis.RedisCache;
import com.zsc.common.utils.StringUtils;
import com.zsc.common.utils.http.UserAgentUtils;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.domain.entity.SlShortLink;
import com.zsc.module.mapper.SlShortLinkMapper;
import com.zsc.module.service.ShortLinkRedirectService;
import com.zsc.module.service.SlClickLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 短链接重定向服务实现（含Redis缓存加速）
 * </p>
 *
 * 缓存策略：Cache-Aside
 * - 读：先查 Redis → 未命中则查 DB → 写入 Redis（TTL 24小时）
 * - 失效：CRUD 更新/删除时调用 {@link #evictCache(String)} 清除缓存
 * - 防穿透：即使 DB 查不到也写入空值缓存（较短 TTL），防止恶意短码遍历
 *
 * @author author
 * @since 2026-06-19
 */
@Service
public class ShortLinkRedirectServiceImpl implements ShortLinkRedirectService {

    /** 缓存键前缀 */
    private static final String CACHE_KEY_PREFIX = "shortlink:code:";

    /** 缓存 TTL：24 小时 */
    private static final long CACHE_TTL_HOURS = 24;

    /** 空值缓存 TTL：5 分钟（防缓存穿透） */
    private static final long NULL_CACHE_TTL_MINUTES = 5;

    /** 状态：正常 */
    private static final String STATUS_NORMAL = "0";

    /** 健康状态：异常 */
    private static final String HEALTH_ABNORMAL = "1";

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SlShortLinkMapper shortLinkMapper;

    @Autowired
    private SlClickLogService clickLogService;

    @Override
    public String redirect(String shortCode, String password, String ip,
                           String userAgent, String referer) {
        // 1. 查询短链接（缓存 + DB）
        SlShortLink link = getByShortCode(shortCode);
        if (link == null) {
            return null;
        }

        // 2. 校验状态
        if (!STATUS_NORMAL.equals(link.getStatus())) {
            return null;
        }

        // 3. 校验健康状态
        if (HEALTH_ABNORMAL.equals(link.getHealthStatus())) {
            return null;
        }

        // 4. 校验过期时间
        if (link.getExpireTime() != null && link.getExpireTime().before(new Date())) {
            return null;
        }

        // 5. 校验最大点击次数
        if (link.getMaxClicks() != null && link.getClickCount() != null
                && link.getClickCount() >= link.getMaxClicks()) {
            return null;
        }

        // 6. 校验访问密码
        if (StringUtils.isNotBlank(link.getAccessPassword())) {
            if (StringUtils.isBlank(password) || !link.getAccessPassword().equals(password)) {
                throw new ServiceException("访问密码错误");
            }
        }

        // ---------- 以上校验通过，执行业务操作 ----------

        // 7. 同步更新点击量
        Long newCount = (link.getClickCount() == null ? 0L : link.getClickCount()) + 1;
        shortLinkMapper.updateClickCount(link.getId(), newCount);

        // 8. 异步记录点击日志（SlClickLogServiceImpl.logClick() 上有 @Async，自动异步执行）
        String os = UserAgentUtils.getOperatingSystem(userAgent);
        String browser = UserAgentUtils.getBrowser(userAgent);
        clickLogService.logClick(link.getId(), ip, userAgent, referer, os, browser);

        return link.getOriginalUrl();
    }

    @Override
    public void evictCache(String shortCode) {
        redisCache.deleteObject(buildCacheKey(shortCode));
    }

    // ==================== 私有方法 ====================

    /**
     * 根据短码查询短链接（Cache-Aside 模式）
     */
    private SlShortLink getByShortCode(String shortCode) {
        String cacheKey = buildCacheKey(shortCode);

        // 1. 查缓存
        SlShortLink link = redisCache.getCacheObject(cacheKey);
        if (link != null) {
            return link;
        }

        // 2. 缓存未命中，查 DB
        link = shortLinkMapper.selectOne(
                new LambdaQueryWrapper<SlShortLink>()
                        .eq(SlShortLink::getShortCode, shortCode)
        );

        // 3. 写入缓存
        if (link != null) {
            // 正常数据：24 小时 TTL
            redisCache.setCacheObject(cacheKey, link, (int) CACHE_TTL_HOURS, TimeUnit.HOURS);
        } else {
            // 空值缓存：5 分钟 TTL（防缓存穿透）
            redisCache.setCacheObject(cacheKey, null, (int) NULL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }

        return link;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String shortCode) {
        return CACHE_KEY_PREFIX + shortCode;
    }
}
