package com.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.BusinessException;
import com.shortlink.dto.CreateShortLinkDTO;
import com.shortlink.dto.ShortLinkVO;
import com.shortlink.dto.UpdateShortLinkDTO;
import com.shortlink.entity.ShortLink;
import com.shortlink.mapper.ShortLinkMapper;
import com.shortlink.service.ShortLinkService;
import com.shortlink.utils.Base62Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${short-link.domain}")
    private String domain;

    private static final String COUNTER_KEY = "short_link:counter";
    private static final String CODE_PREFIX = "short_link:code:";

    @Override
    public ShortLinkVO create(CreateShortLinkDTO dto, Long userId) {
        String shortCode = generateShortCode(dto.getOriginalUrl());
        ShortLink entity = new ShortLink();
        entity.setShortCode(shortCode);
        entity.setOriginalUrl(dto.getOriginalUrl());
        entity.setUserId(userId);
        entity.setExpireTime(dto.getExpireTime());
        entity.setStatus(1);
        shortLinkMapper.insert(entity);
        stringRedisTemplate.opsForValue().set(CODE_PREFIX + shortCode,
                dto.getOriginalUrl(), 7, TimeUnit.DAYS);
        return toVO(entity);
    }

    @Override
    public Page<ShortLinkVO> page(int pageNum, int pageSize, Long userId) {
        Page<ShortLink> page = new Page<>(pageNum, pageSize);
        Page<ShortLink> result = shortLinkMapper.selectPage(page,
                new LambdaQueryWrapper<ShortLink>()
                        .eq(ShortLink::getUserId, userId)
                        .orderByDesc(ShortLink::getCreatedAt));
        Page<ShortLinkVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public ShortLinkVO getById(Long id) {
        ShortLink entity = shortLinkMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("短链接不存在");
        }
        return toVO(entity);
    }

    @Override
    public ShortLinkVO getByShortCode(String shortCode) {
        ShortLink entity = shortLinkMapper.selectOne(
                new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, shortCode));
        if (entity == null) {
            throw new BusinessException("短链接不存在");
        }
        return toVO(entity);
    }

    @Override
    public void update(Long id, UpdateShortLinkDTO dto, Long userId) {
        ShortLink entity = shortLinkMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("短链接不存在");
        }
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该短链接");
        }
        entity.setOriginalUrl(dto.getOriginalUrl());
        entity.setExpireTime(dto.getExpireTime());
        entity.setStatus(dto.getStatus());
        shortLinkMapper.updateById(entity);
        stringRedisTemplate.delete(CODE_PREFIX + entity.getShortCode());
    }

    @Override
    public void delete(Long id, Long userId) {
        ShortLink entity = shortLinkMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("短链接不存在");
        }
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该短链接");
        }
        shortLinkMapper.deleteById(id);
        stringRedisTemplate.delete(CODE_PREFIX + entity.getShortCode());
    }

    @Override
    public String getOriginalUrlAndIncrement(String shortCode) {
        String cached = stringRedisTemplate.opsForValue().get(CODE_PREFIX + shortCode);
        if (cached != null) {
            ShortLink entity = shortLinkMapper.selectOne(
                    new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, shortCode));
            if (entity != null) {
                shortLinkMapper.incrementClickCount(entity.getId());
            }
            return cached;
        }
        ShortLink entity = shortLinkMapper.selectOne(
                new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, shortCode));
        if (entity == null || entity.getStatus() != 1) {
            throw new BusinessException(404, "短链接不存在或已失效");
        }
        if (entity.getExpireTime() != null
                && entity.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(404, "短链接已过期");
        }
        shortLinkMapper.incrementClickCount(entity.getId());
        stringRedisTemplate.opsForValue().set(CODE_PREFIX + shortCode,
                entity.getOriginalUrl(), 7, TimeUnit.DAYS);
        return entity.getOriginalUrl();
    }

    private String generateShortCode(String url) {
        Long counter = stringRedisTemplate.opsForValue().increment(COUNTER_KEY, 1);
        String code = Base62Utils.encode(counter + 100000000L);
        Long exists = shortLinkMapper.selectCount(
                new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, code));
        if (exists > 0) {
            code = Base62Utils.encode(counter + System.nanoTime() % 1000000);
        }
        return code;
    }

    private ShortLinkVO toVO(ShortLink entity) {
        return ShortLinkVO.builder()
                .id(entity.getId())
                .shortCode(entity.getShortCode())
                .shortUrl(domain + "/" + entity.getShortCode())
                .originalUrl(entity.getOriginalUrl())
                .userId(entity.getUserId())
                .clickCount(entity.getClickCount())
                .expireTime(entity.getExpireTime())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
