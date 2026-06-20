package com.zsc.module.service;

/**
 * <p>
 * 短链接重定向服务接口（用于公开重定向场景，含Redis缓存）
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
public interface ShortLinkRedirectService {

    /**
     * 短链接重定向 - 先查缓存再查DB，校验有效性后返回原始URL
     *
     * @param shortCode 短码
     * @param password  访问密码（可空）
     * @param ip        请求IP
     * @param userAgent User-Agent
     * @param referer   来源地址
     * @return 原始URL（校验失败返回null，密码错误抛出异常）
     * @throws com.zsc.module.common.exception.ServiceException 密码错误时抛出
     */
    String redirect(String shortCode, String password, String ip, String userAgent, String referer);

    /**
     * 清除指定短码的缓存（CRUD操作后调用）
     */
    void evictCache(String shortCode);
}
