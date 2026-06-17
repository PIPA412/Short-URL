package com.shortlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.entity.AccessLog;

public interface AccessLogService {

    void record(Long shortLinkId, String ip, String userAgent, String referer);

    Page<AccessLog> page(int pageNum, int pageSize, Long shortLinkId);
}
