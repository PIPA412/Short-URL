package com.zsc.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.SlShortLinkDto;
import com.zsc.module.domain.dto.query.SlShortLinkQueryDto;
import com.zsc.module.domain.entity.SlShortLink;

import java.util.List;
import java.util.Map;

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
     * 查询指定短链接的每日点击统计数据
     * @param linkId 链接ID
     * @return 每日点击统计列表
     */
    List<Map<String, Object>> getClickStats(Long linkId);
}
