package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.constant.CacheConstants;
import com.zsc.common.core.redis.RedisCache;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.common.utils.ServletUtils;
import com.zsc.common.utils.StringUtils;
import com.zsc.common.utils.ip.IpUtils;
import com.zsc.framework.web.service.SimpleCaptchaService;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.SlShortLinkDto;
import com.zsc.module.domain.dto.query.SlShortLinkQueryDto;
import com.zsc.module.domain.entity.SlShortLink;
import com.zsc.module.mapper.SlShortLinkMapper;
import com.zsc.module.service.BlacklistDomainService;
import com.zsc.module.service.SlShortLinkService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 短链接表 服务实现类（CRUD 操作）
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
@Service
@Transactional
public class SlShortLinkServiceImpl extends ServiceImpl<SlShortLinkMapper, SlShortLink> implements SlShortLinkService {

    private static final String CACHE_KEY_PREFIX = "shortlink:code:";

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private BlacklistDomainService blacklistDomainService;

    @Autowired
    private SimpleCaptchaService simpleCaptchaService;

    /** 同一 IP 连续生成短链接触发验证码的阈值 */
    private static final int CAPTCHA_THRESHOLD = 10;

    /** 连续生成计数器 TTL（分钟） */
    private static final int CAPTCHA_COUNTER_TTL_MINUTES = 60;

    /**
     * 添加短链接
     */
    @Override
    public String addLink(SlShortLinkDto addDto) {
        SlShortLink link = new SlShortLink();

        // 将DTO数据复制到实体类
        BeanUtils.copyProperties(addDto, link);

        // 校验过期时间不能早于当前时间
        if (link.getExpireTime() != null && link.getExpireTime().before(new Date())) {
            throw new ServiceException("过期时间不能早于当前时间");
        }

        // 校验域名黑名单
        if (blacklistDomainService.isDomainBlacklisted(link.getOriginalUrl())) {
            throw new ServiceException("该域名已被禁止生成短链接");
        }

        // 同一 IP 连续生成短链接超过阈值时校验图形验证码
        checkCaptchaForShortLink(addDto);

        // 如果未指定短码，则自动生成
        if (StringUtils.isBlank(link.getShortCode())) {
            link.setShortCode(generateShortCode());
        }

        // 设置创建用户ID
        try {
            link.setUserId(SecurityUtils.getUserId());
        } catch (Exception e) {
            // 公开接口（无登录用户）时 userId 留空
        }

        // 设置默认字段
        link.setClickCount(0L);
        link.setCreateTime(new Date());
        if (StringUtils.isBlank(link.getStatus())) {
            link.setStatus("0"); // 默认正常
        }
        if (StringUtils.isBlank(link.getHealthStatus())) {
            link.setHealthStatus("2"); // 默认未知
        }

        // 保存对象
        if (!this.save(link)) {
            throw new ServiceException("系统错误，短链接添加失败！");
        }

        // 生成成功，递增 IP 生成计数器
        incrementIpCounter();

        return link.getShortCode();
    }

    /**
     * 校验短链接生成是否需要图形验证码
     * 同一 IP 连续生成超过阈值（默认 10 次）后触发
     */
    private void checkCaptchaForShortLink(SlShortLinkDto dto) {
        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        String counterKey = CacheConstants.SHORT_LINK_COUNTER_KEY + ip;
        Integer count = redisCache.getCacheObject(counterKey);

        if (count != null && count >= CAPTCHA_THRESHOLD) {
            // IP 已达阈值，需校验验证码
            if (StringUtils.isBlank(dto.getCaptchaCode()) || StringUtils.isBlank(dto.getCaptchaUuid())) {
                throw new ServiceException("操作频繁，请提供图形验证码");
            }
            boolean passed = simpleCaptchaService.verifyCaptcha(dto.getCaptchaUuid(), dto.getCaptchaCode());
            if (!passed) {
                throw new ServiceException("验证码错误或已过期");
            }
            // 验证通过，重置计数器
            redisCache.deleteObject(counterKey);
        }
    }

    /**
     * 递增 IP 短链接生成计数器
     */
    private void incrementIpCounter() {
        try {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            String counterKey = CacheConstants.SHORT_LINK_COUNTER_KEY + ip;
            redisCache.incrementAndExpire(counterKey, 1, CAPTCHA_COUNTER_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 计数器异常不影响主流程
        }
    }

    /**
     * 更新短链接（更新后清除缓存）
     */
    @Override
    public void updateLink(SlShortLinkDto updateDto) {
        if (updateDto.getId() == null) {
            throw new ServiceException("链接ID不能为空");
        }

        SlShortLink link = new SlShortLink();

        // 将DTO转换为实体类
        BeanUtils.copyProperties(updateDto, link);

        // 校验过期时间不能早于当前时间
        if (link.getExpireTime() != null && link.getExpireTime().before(new Date())) {
            throw new ServiceException("过期时间不能早于当前时间");
        }

        // 进行增量更新（仅更新非 null 字段）
        if (!this.updateById(link)) {
            throw new ServiceException("系统错误，短链接更新失败！");
        }

        // 清除缓存
        evictCache(updateDto.getShortCode());
        // 如果短码变更，还需要清除旧的短码缓存
        SlShortLink oldLink = this.getById(updateDto.getId());
        if (oldLink != null && !oldLink.getShortCode().equals(updateDto.getShortCode())) {
            evictCache(oldLink.getShortCode());
        }
    }

    /**
     * 复杂条件查询
     */
    @Override
    public PageResult<SlShortLink> queryLinks(SlShortLinkQueryDto queryDto) {
        // 解析日期范围
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginDate = null, endDate = null;
        try {
            if (StringUtils.isNotBlank(queryDto.getBeginTime())) {
                beginDate = sdf.parse(queryDto.getBeginTime() + " 00:00:00");
            }
            if (StringUtils.isNotBlank(queryDto.getEndTime())) {
                endDate = sdf.parse(queryDto.getEndTime() + " 23:59:59");
            }
        } catch (ParseException e) {
            // ignore parse errors
        }

        // 构建查询条件
        LambdaQueryWrapper<SlShortLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryDto.getShortCode()),
                     SlShortLink::getShortCode, queryDto.getShortCode())
               .like(StringUtils.isNotBlank(queryDto.getOriginalUrl()),
                     SlShortLink::getOriginalUrl, queryDto.getOriginalUrl())
               .eq(StringUtils.isNotBlank(queryDto.getStatus()),
                   SlShortLink::getStatus, queryDto.getStatus())
               .eq(queryDto.getUserId() != null,
                   SlShortLink::getUserId, queryDto.getUserId())
               .ge(beginDate != null, SlShortLink::getCreateTime, beginDate)
               .le(endDate != null, SlShortLink::getCreateTime, endDate);

        // 非管理员只能看到自己创建的短链接
        if (!SecurityUtils.isAdmin()) {
            try {
                wrapper.eq(SlShortLink::getUserId, SecurityUtils.getUserId());
            } catch (Exception e) {
                // 无登录信息时不做过滤
            }
        }

        wrapper.orderByDesc(SlShortLink::getCreateTime);

        Page<SlShortLink> result = this.page(queryDto.convetToPage(), wrapper);

        return PageResult.fromPage(result);
    }

    /**
     * 获取短链接统计数据
     */
    @Override
    public Map<String, Object> getLinkStats(Long linkId) {
        SlShortLink link = this.getById(linkId);
        if (link == null) {
            throw new ServiceException("短链接不存在");
        }

        // 非管理员只能查看自己的链接统计
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId;
            try {
                currentUserId = SecurityUtils.getUserId();
            } catch (Exception e) {
                throw new ServiceException("无权查看该链接统计");
            }
            if (!currentUserId.equals(link.getUserId())) {
                throw new ServiceException("无权查看该链接统计");
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("id", link.getId());
        stats.put("shortCode", link.getShortCode());
        stats.put("originalUrl", link.getOriginalUrl());
        stats.put("clickCount", link.getClickCount());
        stats.put("status", link.getStatus());
        stats.put("healthStatus", link.getHealthStatus());
        stats.put("createTime", link.getCreateTime());
        stats.put("expireTime", link.getExpireTime());
        stats.put("userId", link.getUserId());

        // 获取该用户的总链接数和总点击量
        if (link.getUserId() != null) {
            Long userTotalLinks = this.lambdaQuery()
                    .eq(SlShortLink::getUserId, link.getUserId())
                    .count();
            Long userTotalClicks = this.lambdaQuery()
                    .eq(SlShortLink::getUserId, link.getUserId())
                    .select(SlShortLink::getClickCount)
                    .list()
                    .stream().mapToLong(sl -> sl.getClickCount() == null ? 0L : sl.getClickCount()).sum();
            stats.put("userTotalLinks", userTotalLinks);
            stats.put("userTotalClicks", userTotalClicks);
        }

        return stats;
    }

    /**
     * 重写删除：删除后清除缓存
     */
    @Override
    public boolean removeById(Serializable id) {
        // 删除前先查询，获取 shortCode 用于清除缓存
        SlShortLink link = this.getById(id);
        if (link != null) {
            evictCache(link.getShortCode());
        }
        return super.removeById(id);
    }

    /**
     * 生成唯一的6位短码
     */
    private String generateShortCode() {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            String code = RandomStringUtils.randomAlphanumeric(6);
            long count = this.lambdaQuery()
                    .eq(SlShortLink::getShortCode, code)
                    .count();
            if (count == 0) {
                return code;
            }
        }
        throw new ServiceException("短码生成失败，请稍后再试");
    }

    /**
     * 清除短链接缓存
     */
    private void evictCache(String shortCode) {
        if (StringUtils.isNotBlank(shortCode)) {
            redisCache.deleteObject(CACHE_KEY_PREFIX + shortCode);
        }
    }
}
