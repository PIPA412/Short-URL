package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.domain.entity.SlClickLog;
import com.zsc.module.mapper.SlClickLogMapper;
import com.zsc.module.service.SlClickLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 点击日志表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-19
 */
@Service
public class SlClickLogServiceImpl extends ServiceImpl<SlClickLogMapper, SlClickLog> implements SlClickLogService {

    /**
     * 记录点击日志（异步执行，不阻塞重定向）
     */
    @Async
    @Override
    public void logClick(Long shortLinkId, String ip, String userAgent, String referer,
                         String deviceType, String browser) {
        SlClickLog log = SlClickLog.builder()
                .shortLinkId(shortLinkId)
                .clickTime(new Date())
                .ip(ip)
                .userAgent(userAgent)
                .referer(referer)
                .deviceType(deviceType)
                .browser(browser)
                .build();

        this.save(log);
    }

}
