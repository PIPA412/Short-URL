package com.shortlink.system.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.common.constant.CacheConstants;
import com.shortlink.framework.cache.BloomFilterHelper;
import com.shortlink.system.entity.ShortLink;
import com.shortlink.system.mapper.ShortLinkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Cache pre-warming component.
 * <p>
 * Runs after application startup ({@link ApplicationRunner}) to:
 * <ol>
 *   <li>Load recently created (7 days) / popular (high click count) short links</li>
 *   <li>Write shortCode → originalUrl mappings into Redis</li>
 *   <li>Populate the Bloom Filter with all existing short codes</li>
 * </ol>
 * <p>
 * This reduces cache-miss penalty for the first requests after a restart.
 * <p>
 * Located in system module because it depends on {@link ShortLinkMapper}.
 *
 * @author ShortLink
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class CacheWarmUp implements ApplicationRunner {

    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate redisTemplate;
    private final BloomFilterHelper bloomFilter;

    /** Maximum number of links to pre-warm (prevent OOM on large datasets) */
    private static final int WARM_UP_LIMIT = 500;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== Cache Pre-Warming Started ==========");
        try {
            warmUpHotLinks();
            warmUpBloomFilter();
        } catch (Exception e) {
            log.error("Cache pre-warming failed (app continues): {}", e.getMessage(), e);
        }
        log.info("========== Cache Pre-Warming Completed ==========");
    }

    private void warmUpHotLinks() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<ShortLink> recentLinks = shortLinkMapper.selectList(
                new LambdaQueryWrapper<ShortLink>()
                        .eq(ShortLink::getStatus, 0)
                        .ge(ShortLink::getCreateTime, sevenDaysAgo)
                        .orderByDesc(ShortLink::getClickCount)
                        .last("LIMIT " + WARM_UP_LIMIT)
        );

        if (recentLinks.isEmpty()) {
            log.info("No recent links to pre-warm.");
            return;
        }

        int warmed = 0;
        for (ShortLink link : recentLinks) {
            String cacheKey = CacheConstants.SHORT_CODE_KEY + link.getShortCode();
            Boolean absent = redisTemplate.opsForValue()
                    .setIfAbsent(cacheKey, link.getOriginalUrl(),
                            CacheConstants.WARM_CACHE_TTL_HOURS, TimeUnit.HOURS);
            if (Boolean.TRUE.equals(absent)) warmed++;
        }
        log.info("Pre-warmed {} short links (out of {} candidates)", warmed, recentLinks.size());
    }

    private void warmUpBloomFilter() {
        List<ShortLink> allLinks = shortLinkMapper.selectList(
                new LambdaQueryWrapper<ShortLink>()
                        .select(ShortLink::getShortCode)
                        .eq(ShortLink::getStatus, 0)
        );
        if (allLinks.isEmpty()) return;

        List<String> codes = allLinks.stream()
                .map(ShortLink::getShortCode)
                .collect(Collectors.toList());
        bloomFilter.warmUp(codes);
    }
}
