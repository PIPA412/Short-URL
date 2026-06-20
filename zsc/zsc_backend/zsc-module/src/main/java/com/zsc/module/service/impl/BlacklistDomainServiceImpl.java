package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.domain.entity.SlBlacklistDomain;
import com.zsc.module.mapper.SlBlacklistDomainMapper;
import com.zsc.module.service.BlacklistDomainService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 黑名单域名表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
@Service
public class BlacklistDomainServiceImpl extends ServiceImpl<SlBlacklistDomainMapper, SlBlacklistDomain>
        implements BlacklistDomainService {

    /**
     * 主域名提取正则：匹配 example.com 或 example.com.cn 形式的域名主体
     */
    private static final Pattern DOMAIN_PATTERN =
            Pattern.compile("([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+)$");

    @Override
    public void addDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            throw new ServiceException("域名不能为空");
        }

        String cleanDomain = domain.trim().toLowerCase();

        // 检查是否已存在
        long count = this.lambdaQuery()
                .eq(SlBlacklistDomain::getDomain, cleanDomain)
                .count();
        if (count > 0) {
            throw new ServiceException("该域名已在黑名单中");
        }

        SlBlacklistDomain entity = SlBlacklistDomain.builder()
                .domain(cleanDomain)
                .createTime(new Date())
                .build();

        if (!this.save(entity)) {
            throw new ServiceException("添加黑名单域名失败");
        }
    }

    @Override
    public void removeDomain(Long id) {
        if (!this.removeById(id)) {
            throw new ServiceException("黑名单域名不存在");
        }
    }

    @Override
    public List<SlBlacklistDomain> listDomains() {
        return this.lambdaQuery()
                .orderByDesc(SlBlacklistDomain::getCreateTime)
                .list();
    }

    @Override
    public boolean isDomainBlacklisted(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        String mainDomain = extractMainDomain(url);
        if (mainDomain == null) {
            return false;
        }

        // 查询黑名单（使用缓存友好的全表查询）
        List<SlBlacklistDomain> blacklist = this.list();

        for (SlBlacklistDomain item : blacklist) {
            String blackDomain = item.getDomain().toLowerCase();

            // 精确匹配：example.com == example.com
            if (mainDomain.equals(blackDomain)) {
                return true;
            }

            // 子域名匹配：sub.example.com 匹配 example.com
            if (mainDomain.endsWith("." + blackDomain)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String extractMainDomain(String url) {
        try {
            // 规范化：如果没有协议头，添加 https://
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                return null;
            }

            host = host.toLowerCase();

            // 去掉 www. 前缀
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            return host;
        } catch (Exception e) {
            // URI 解析失败，使用正则兜底
            Matcher matcher = DOMAIN_PATTERN.matcher(url.toLowerCase());
            if (matcher.find()) {
                String domain = matcher.group(1);
                // 去掉 www. 前缀
                if (domain.startsWith("www.")) {
                    domain = domain.substring(4);
                }
                return domain;
            }
            return null;
        }
    }
}
