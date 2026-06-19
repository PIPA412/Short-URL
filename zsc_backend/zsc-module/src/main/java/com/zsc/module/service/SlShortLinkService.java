package com.zsc.module.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.SlShortLinkDto;
import com.zsc.module.domain.dto.query.SlShortLinkQueryDto;
import com.zsc.module.domain.entity.SlShortLink;

/**
 * <p>
 * 短链接表 服务类
 * </p>
 *
 * @author author
 * @since 2026-06-16
 */
public interface SlShortLinkService extends IService<SlShortLink> {

    /**
     * 添加短链接
     * @return 生成的短码
     */
    String addLink(SlShortLinkDto addDto);

    /**
     * 更新短链接
     */
    void updateLink(SlShortLinkDto updateDto);

    /**
     * 复杂条件查询，包含分页信息
     */
    PageResult<SlShortLink> queryLinks(SlShortLinkQueryDto queryDto);

    /**
     * 短链接重定向 - 根据短码查找原始URL并增加点击量
     * @return 原始URL，如果短链接不存在或已禁用/过期则返回null
     */
    String redirect(String shortCode);

    /**
     * 获取短链接统计数据
     * @param linkId 链接ID
     * @return 统计数据Map
     */
    Map<String, Object> getLinkStats(Long linkId);
}
