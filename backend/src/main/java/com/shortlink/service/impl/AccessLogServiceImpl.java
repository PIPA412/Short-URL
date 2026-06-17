package com.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.entity.AccessLog;
import com.shortlink.mapper.AccessLogMapper;
import com.shortlink.service.AccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogMapper accessLogMapper;

    @Override
    public void record(Long shortLinkId, String ip, String userAgent, String referer) {
        AccessLog log = new AccessLog();
        log.setShortLinkId(shortLinkId);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setReferer(referer);
        log.setAccessTime(LocalDateTime.now());
        accessLogMapper.insert(log);
    }

    @Override
    public Page<AccessLog> page(int pageNum, int pageSize, Long shortLinkId) {
        Page<AccessLog> page = new Page<>(pageNum, pageSize);
        return accessLogMapper.selectPage(page,
                new LambdaQueryWrapper<AccessLog>()
                        .eq(AccessLog::getShortLinkId, shortLinkId)
                        .orderByDesc(AccessLog::getAccessTime));
    }
}
