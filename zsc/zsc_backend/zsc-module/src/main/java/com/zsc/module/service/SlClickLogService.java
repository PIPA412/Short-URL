package com.zsc.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.domain.entity.SlClickLog;

/**
 * <p>
 * 点击日志表 服务类
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
public interface SlClickLogService extends IService<SlClickLog> {

    /**
     * 记录点击日志
     *
     * @param shortLinkId 短链接ID
     * @param ip          请求IP
     * @param userAgent   浏览器 UA
     * @param referer     来源地址
     * @param deviceType  设备类型
     * @param browser     浏览器名称
     */
    void logClick(Long shortLinkId, String ip, String userAgent, String referer,
                  String deviceType, String browser);

}
