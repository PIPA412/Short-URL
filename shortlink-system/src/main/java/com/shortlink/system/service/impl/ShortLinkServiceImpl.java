package com.shortlink.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.config.ShortLinkConfig;
import com.shortlink.common.constant.CacheConstants;
import com.shortlink.common.enums.LinkStatusEnum;
import com.shortlink.common.exception.ForbiddenException;
import com.shortlink.common.exception.NotFoundException;
import com.shortlink.common.utils.ShortCodeGenerator;
import com.shortlink.framework.cache.BloomFilterHelper;
import com.shortlink.system.dto.req.ShortLinkCreateRequest;
import com.shortlink.system.dto.req.ShortLinkPageQuery;
import com.shortlink.system.dto.req.ShortLinkUpdateRequest;
import com.shortlink.system.dto.resp.ShortLinkPageItem;
import com.shortlink.system.dto.resp.ShortLinkResponse;
import com.shortlink.system.entity.ShortLink;
import com.shortlink.system.mapper.ShortLinkMapper;
import com.shortlink.system.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Short link service implementation.
 * <p>
 * <b>Caching strategy (3-tier defense):</b>
 * <ol>
 *   <li><b>Bloom Filter</b> — probabilistic check: "definitely not exist?"
 *       If no → return null immediately (no Redis, no DB).</li>
 *   <li><b>Redis Cache</b> — key-value lookup:
 *       {@code short:code:{code}} → original URL (24h TTL).</li>
 *   <li><b>Database</b> — source of truth, queried only on cache miss.</li>
 *   <li><b>Null Marker</b> — penetration guard: non-existent codes are
 *       cached as {@code \0__NULL__} with 5-min TTL to prevent
 *       repeated DB queries for phantom keys.</li>
 * </ol>
 *
 * @author ShortLink
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkConfig shortLinkConfig;
    private final StringRedisTemplate redisTemplate;
    private final BloomFilterHelper bloomFilter;

    // ==================== CREATE ====================

    @Override
    @Transactional
    public ShortLinkResponse create(Long userId, ShortLinkCreateRequest request) {
        String shortCode = generateUniqueShortCode();

        ShortLink link = new ShortLink();
        link.setUserId(userId);
        link.setShortCode(shortCode);
        link.setOriginalUrl(request.getOriginalUrl());
        link.setTitle(request.getTitle());
        link.setDescription(request.getDescription());
        link.setExpireTime(resolveExpireTime(request));
        link.setClickCount(0L);
        link.setStatus(LinkStatusEnum.NORMAL.getCode());

        shortLinkMapper.insert(link);

        // Populate Redis caches
        cacheShortCode(shortCode, request.getOriginalUrl());
        // Add to Bloom Filter for future penetration protection
        bloomFilter.add(shortCode);

        log.info("Short link created: id={}, shortCode={}, userId={}",
                link.getId(), shortCode, userId);
        return toResponse(link);
    }

    // ==================== UPDATE ====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "short-link", key = "#existing.shortCode"),
            @CacheEvict(value = "short-link-id", key = "#request.id")
    })
    public void update(Long userId, ShortLinkUpdateRequest request) {
        ShortLink link = getEntityById(request.getId());
        ShortLink existing = link; // captured for CacheEvict key expression

        if (!link.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only update your own short links");
        }

        String oldCode = link.getShortCode();

        if (StringUtils.hasText(request.getOriginalUrl())) {
            link.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getTitle() != null) {
            link.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            link.setDescription(request.getDescription());
        }
        if (request.getExpireTime() != null) {
            link.setExpireTime(request.getExpireTime());
        }
        if (request.getStatus() != null) {
            link.setStatus(request.getStatus());
        }

        shortLinkMapper.updateById(link);

        // Refresh the short-code → URL cache
        cacheShortCode(oldCode, link.getOriginalUrl());

        log.info("Short link updated: id={}, shortCode={}", link.getId(), link.getShortCode());
    }

    // ==================== DELETE ====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "short-link", key = "#link.shortCode"),
            @CacheEvict(value = "short-link-id", key = "#linkId")
    })
    public void delete(Long userId, Long linkId) {
        ShortLink link = getEntityById(linkId);

        if (!link.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own short links");
        }

        shortLinkMapper.deleteById(linkId);
        evictShortCodeCache(link.getShortCode());
        // Note: Bloom Filter is append-only; removal is not possible.
        // False positives ~1% are acceptable — they only cause a cache+DB lookup.

        log.info("Short link deleted: id={}, shortCode={}", linkId, link.getShortCode());
    }

    // ==================== QUERIES ====================

    @Override
    public ShortLinkResponse getById(Long linkId) {
        return toResponse(getEntityById(linkId));
    }

    @Override
    @Cacheable(value = "short-link-id", key = "#linkId", unless = "#result == null")
    public ShortLink getEntityById(Long linkId) {
        ShortLink link = shortLinkMapper.selectById(linkId);
        if (link == null) {
            throw new NotFoundException("Short link not found: id=" + linkId);
        }
        return link;
    }

    @Override
    public IPage<ShortLinkPageItem> pageQuery(Long userId, ShortLinkPageQuery query) {
        Page<ShortLink> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ShortLink> result = shortLinkMapper.selectPageByUserId(
                page, userId, query.getTitle(), query.getStatus());

        List<ShortLinkPageItem> items = result.getRecords().stream()
                .map(this::toPageItem)
                .collect(Collectors.toList());

        Page<ShortLinkPageItem> responsePage =
                new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(items);
        return responsePage;
    }

    /**
     * Resolve the original URL for a short code.
     * <p>
     * <b>3-tier lookup with penetration protection:</b>
     * <ol>
     *   <li><b>Bloom Filter:</b> If the code definitely doesn't exist,
     *       return null immediately (avoids cache- and DB-lookup).</li>
     *   <li><b>Redis cache:</b> Look up {@code short:code:{code}}.
     *       If the value is the {@link CacheConstants#NULL_MARKER},
     *       return null (code confirmed non-existent).</li>
     *   <li><b>Database:</b> Query via MyBatis-Plus. On hit → cache the URL.
     *       On miss → cache the NULL_MARKER (5-min TTL).</li>
     * </ol>
     *
     * @param shortCode the short code to resolve
     * @return original URL, or {@code null} if not found/expired/disabled
     */
    @Override
    public String getOriginalUrlByShortCode(String shortCode) {
        // ---- Layer 0: Bloom Filter ----
        if (!bloomFilter.mightExist(shortCode)) {
            log.debug("BloomFilter: definitely-not-exist for shortCode={}", shortCode);
            return null;
        }

        String cacheKey = CacheConstants.SHORT_CODE_KEY + shortCode;

        // ---- Layer 1: Redis Cache ----
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (CacheConstants.isNullMarker(cached)) {
                // Cached "not found" marker → skip DB
                log.debug("Null-marker cache hit for shortCode={}", shortCode);
                return null;
            }
            // Cache hit with a real URL
            return cached;
        }

        // ---- Layer 2: Database ----
        ShortLink link = getByShortCode(shortCode);

        // ---- Case A: Link does NOT exist → cache null marker ----
        if (link == null) {
            cacheNullMarker(shortCode);
            return null;
        }

        // ---- Case B: Link exists but is expired ----
        if (link.getExpireTime() != null
                && link.getExpireTime().isBefore(LocalDateTime.now())) {
            link.setStatus(LinkStatusEnum.EXPIRED.getCode());
            shortLinkMapper.updateById(link);
            // Don't cache expired links (neither URL nor null marker)
            evictShortCodeCache(shortCode);
            return null;
        }

        // ---- Case C: Link is disabled ----
        if (link.getStatus() == LinkStatusEnum.DISABLED.getCode()) {
            // Disabled links: cache null marker so we don't query DB repeatedly
            cacheNullMarker(shortCode);
            return null;
        }

        // ---- Case D: Valid link → cache URL ----
        cacheShortCode(shortCode, link.getOriginalUrl());
        return link.getOriginalUrl();
    }

    /**
     * Look up a ShortLink entity by short code.
     * Uses {@code @Cacheable} to cache entities in Redis (24h TTL).
     */
    @Override
    @Cacheable(value = "short-link", key = "#shortCode", unless = "#result == null")
    public ShortLink getByShortCode(String shortCode) {
        return shortLinkMapper.selectOne(
                new LambdaQueryWrapper<ShortLink>()
                        .eq(ShortLink::getShortCode, shortCode)
        );
    }

    // ==================== CACHE HELPERS ====================

    /**
     * Cache a shortCode → originalUrl mapping in Redis.
     * TTL: 24 hours (refreshed on update and on cache-hit in redirect flow).
     */
    private void cacheShortCode(String shortCode, String originalUrl) {
        String cacheKey = CacheConstants.SHORT_CODE_KEY + shortCode;
        redisTemplate.opsForValue().set(cacheKey, originalUrl,
                CacheConstants.CODE_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * Cache a NULL_MARKER for a non-existent/disabled short code.
     * TTL: 5 minutes — short enough to pick up changes, long enough
     * to absorb repeated attack/scanning traffic.
     */
    private void cacheNullMarker(String shortCode) {
        String cacheKey = CacheConstants.SHORT_CODE_KEY + shortCode;
        redisTemplate.opsForValue().set(cacheKey, CacheConstants.NULL_MARKER,
                CacheConstants.NULL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Null-marker cached for shortCode={}", shortCode);
    }

    /**
     * Remove a shortCode → URL mapping from Redis.
     */
    private void evictShortCodeCache(String shortCode) {
        String cacheKey = CacheConstants.SHORT_CODE_KEY + shortCode;
        redisTemplate.delete(cacheKey);
    }

    // ==================== CODE GENERATION ====================

    private String generateUniqueShortCode() {
        String code;
        int attempts = 0;
        do {
            code = ShortCodeGenerator.generate();
            if (attempts++ > 5) {
                throw new RuntimeException(
                        "Failed to generate unique short code after " + attempts + " attempts");
            }
        } while (getByShortCode(code) != null);
        return code;
    }

    private LocalDateTime resolveExpireTime(ShortLinkCreateRequest request) {
        if (request.getExpireTime() != null) {
            return request.getExpireTime();
        }
        if (shortLinkConfig.getDefaultExpireDays() > 0) {
            return LocalDateTime.now().plusDays(shortLinkConfig.getDefaultExpireDays());
        }
        return null; // never expires
    }

    // ==================== DTO MAPPERS ====================

    private ShortLinkResponse toResponse(ShortLink link) {
        return ShortLinkResponse.builder()
                .id(link.getId())
                .shortCode(link.getShortCode())
                .shortUrl(shortLinkConfig.getBaseUrl() + "/" + link.getShortCode())
                .originalUrl(link.getOriginalUrl())
                .title(link.getTitle())
                .description(link.getDescription())
                .expireTime(formatDateTime(link.getExpireTime()))
                .clickCount(link.getClickCount())
                .status(link.getStatus())
                .statusDesc(LinkStatusEnum.fromCode(link.getStatus()).getDesc())
                .createTime(formatDateTime(link.getCreateTime()))
                .updateTime(formatDateTime(link.getUpdateTime()))
                .build();
    }

    private ShortLinkPageItem toPageItem(ShortLink link) {
        return ShortLinkPageItem.builder()
                .id(link.getId())
                .shortCode(link.getShortCode())
                .shortUrl(shortLinkConfig.getBaseUrl() + "/" + link.getShortCode())
                .originalUrl(link.getOriginalUrl())
                .title(link.getTitle())
                .expireTime(formatDateTime(link.getExpireTime()))
                .clickCount(link.getClickCount())
                .status(link.getStatus())
                .statusDesc(LinkStatusEnum.fromCode(link.getStatus()).getDesc())
                .createTime(formatDateTime(link.getCreateTime()))
                .build();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
