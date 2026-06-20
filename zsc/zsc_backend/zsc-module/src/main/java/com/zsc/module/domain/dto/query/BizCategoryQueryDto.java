package com.zsc.module.domain.dto.query;

import com.zsc.module.common.pagination.BasePageReq;
import lombok.Data;

/**
 * 业务类别查询DTO
 */
@Data
public class BizCategoryQueryDto extends BasePageReq {

    /**
     * 类别名称
     */
    private String categoryName;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

}
