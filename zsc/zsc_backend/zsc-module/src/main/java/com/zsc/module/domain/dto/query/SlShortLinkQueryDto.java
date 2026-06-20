package com.zsc.module.domain.dto.query;

import com.zsc.module.common.pagination.BasePageReq;
import lombok.Data;

/**
 * 短链接查询DTO
 */
@Data
public class SlShortLinkQueryDto extends BasePageReq {

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 状态（0-正常 1-过期 2-禁用 3-异常）
     */
    private String status;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 创建时间开始
     */
    private String beginTime;

    /**
     * 创建时间结束
     */
    private String endTime;

}
