package com.zsc.framework.interceptor;

import com.zsc.common.constant.CacheConstants;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.redis.RedisCache;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.common.utils.ServletUtils;
import com.zsc.common.utils.StringUtils;
import com.zsc.common.utils.ip.IpUtils;
import com.zsc.system.service.ISysConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 频率限制拦截器 — 短链接生成接口防刷
 * <p>
 * 按用户（每小时最多 N 次）和 IP（每小时最多 M 次）两个维度限制，
 * 超出返回 HTTP 429。
 * 上限通过系统配置表动态配置，管理员可在线修改。
 * </p>
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    /** 用户每小时上限配置键 */
    private static final String CONFIG_KEY_USER_LIMIT = "sys.rate.shortlink.user.hourly";

    /** IP 每小时上限配置键 */
    private static final String CONFIG_KEY_IP_LIMIT = "sys.rate.shortlink.ip.hourly";

    /** 默认用户上限 */
    private static final int DEFAULT_USER_LIMIT = 50;

    /** 默认 IP 上限 */
    private static final int DEFAULT_IP_LIMIT = 100;

    /** 小时桶格式 */
    private static final String HOUR_BUCKET_FORMAT = "yyyyMMddHH";

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysConfigService configService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 仅拦截短链接生成 POST 请求
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (!"POST".equalsIgnoreCase(method)
                || (!path.contains("/api/shortlink") && !path.contains("/api/public/shortlink"))) {
            return true;
        }

        String hourBucket = new SimpleDateFormat(HOUR_BUCKET_FORMAT).format(new Date());
        String ip = IpUtils.getIpAddr(request);

        // 获取配置上限
        int ipLimit = getConfigInt(CONFIG_KEY_IP_LIMIT, DEFAULT_IP_LIMIT);
        int userLimit = getConfigInt(CONFIG_KEY_USER_LIMIT, DEFAULT_USER_LIMIT);

        // ---------- 1. IP 维度限流 ----------
        String ipKey = CacheConstants.RATE_LIMIT_IP_KEY + ip + ":" + hourBucket;
        Long ipCount = redisCache.incrementAndExpire(ipKey, 1, 1, TimeUnit.HOURS);

        if (ipCount != null && ipCount > ipLimit) {
            log.warn("IP 频率限制触发: ip={}, count={}, limit={}", ip, ipCount, ipLimit);
            renderTooManyRequests(response);
            return false;
        }

        // ---------- 2. 用户维度限流（已登录用户） ----------
        try {
            Long userId = SecurityUtils.getUserId();
            String userKey = CacheConstants.RATE_LIMIT_USER_KEY + userId + ":" + hourBucket;
            Long userCount = redisCache.incrementAndExpire(userKey, 1, 1, TimeUnit.HOURS);

            if (userCount != null && userCount > userLimit) {
                log.warn("用户频率限制触发: userId={}, count={}, limit={}", userId, userCount, userLimit);
                renderTooManyRequests(response);
                return false;
            }
        } catch (Exception e) {
            // 未登录用户——IP 限流已足够
        }

        return true;
    }

    /**
     * 返回 429 Too Many Requests
     */
    private void renderTooManyRequests(HttpServletResponse response) throws Exception {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        AjaxResult result = AjaxResult.error("操作过于频繁，请稍后再试");
        response.getWriter().write(com.alibaba.fastjson2.JSON.toJSONString(result));
    }

    /**
     * 从系统配置读取整数值
     */
    private int getConfigInt(String configKey, int defaultValue) {
        try {
            String value = configService.selectConfigByKey(configKey);
            if (StringUtils.isNotBlank(value)) {
                return Integer.parseInt(value.trim());
            }
        } catch (Exception e) {
            log.warn("读取配置 {} 失败，使用默认值 {}", configKey, defaultValue);
        }
        return defaultValue;
    }
}
