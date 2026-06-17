package com.zsc.module.domain.dto.query;

import com.zsc.module.common.pagination.BasePageReq;
import lombok.Data;

/**
 * 短链接查询DTO
 */
@Data
public class SlShortLinkQueryDto extends BasePageReq {

    /**
     * 标题
     */
    private String title;

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 创建时间开始
     */
    private String beginTime;

    /**
     * 创建时间结束
     */
    private String endTime;

}
