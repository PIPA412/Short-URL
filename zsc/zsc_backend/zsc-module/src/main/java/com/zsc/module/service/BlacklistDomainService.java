package com.zsc.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.domain.entity.SlBlacklistDomain;

import java.util.List;

/**
 * <p>
 * 黑名单域名表 服务类
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
public interface BlacklistDomainService extends IService<SlBlacklistDomain> {

    /**
     * 添加黑名单域名
     */
    void addDomain(String domain);

    /**
     * 删除黑名单域名
     */
    void removeDomain(Long id);

    /**
     * 获取黑名单域名列表
     */
    List<SlBlacklistDomain> listDomains();

    /**
     * 检查 URL 是否包含黑名单域名
     *
     * @param url 原始 URL
     * @return true 如果域名为黑名单
     */
    boolean isDomainBlacklisted(String url);

    /**
     * 从 URL 中提取主域名（如 www.example.com/path → example.com）
     */
    String extractMainDomain(String url);
}
