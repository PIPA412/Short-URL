package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.SlShortLinkDto;
import com.zsc.module.domain.dto.query.SlShortLinkQueryDto;
import com.zsc.module.domain.entity.SlShortLink;
import com.zsc.module.mapper.SlShortLinkMapper;
import com.zsc.module.service.SlShortLinkService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * 短链接表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-16
 */
@Service
@Transactional
public class SlShortLinkServiceImpl extends ServiceImpl<SlShortLinkMapper, SlShortLink> implements SlShortLinkService {

    /**
     * 添加短链接
     */
    @Override
    public String addLink(SlShortLinkDto addDto) {
        SlShortLink link = new SlShortLink();

        // 将DTO数据复制到实体类
        BeanUtils.copyProperties(addDto, link);

        // 校验过期时间不能早于当前时间
        if (link.getExpireTime() != null && link.getExpireTime().before(new Date())) {
            throw new ServiceException("过期时间不能早于当前时间");
        }

        // 如果未指定短码，则自动生成
        if (StringUtils.isBlank(link.getShortCode())) {
            link.setShortCode(generateShortCode());
        }

        // 设置创建者
        if (StringUtils.isBlank(link.getCreateBy())) {
            link.setCreateBy(SecurityUtils.getUsername());
        }

        // 设置默认字段
        link.setClicks(0L);
        link.setCreateTime(new Date());
        link.setUpdateTime(new Date());

        // 保存对象
        if (!this.save(link)) {
            throw new ServiceException("系统错误，短链接添加失败！");
        }

        return link.getShortCode();
    }

    /**
     * 更新短链接
     */
    @Override
    public void updateLink(SlShortLinkDto updateDto) {
        SlShortLink link = new SlShortLink();

        // 将DTO转换为实体类
        BeanUtils.copyProperties(updateDto, link);

        // 校验过期时间不能早于当前时间
        if (link.getExpireTime() != null && link.getExpireTime().before(new Date())) {
            throw new ServiceException("过期时间不能早于当前时间");
        }

        // 设置更新时间
        link.setUpdateTime(new Date());

        // 进行增量更新
        if (!this.updateById(link)) {
            throw new ServiceException("系统错误，短链接更新失败！");
        }
    }

    /**
     * 复杂条件查询
     */
    @Override
    public PageResult<SlShortLink> queryLinks(SlShortLinkQueryDto queryDto) {
        // 解析日期范围
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginDate = null, endDate = null;
        try {
            if (StringUtils.isNotBlank(queryDto.getBeginTime())) {
                beginDate = sdf.parse(queryDto.getBeginTime() + " 00:00:00");
            }
            if (StringUtils.isNotBlank(queryDto.getEndTime())) {
                endDate = sdf.parse(queryDto.getEndTime() + " 23:59:59");
            }
        } catch (ParseException e) {
            // ignore parse errors
        }

        // 构建查询条件
        LambdaQueryWrapper<SlShortLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryDto.getTitle()),
                     SlShortLink::getTitle, queryDto.getTitle())
               .like(StringUtils.isNotBlank(queryDto.getShortCode()),
                     SlShortLink::getShortCode, queryDto.getShortCode())
               .like(StringUtils.isNotBlank(queryDto.getOriginalUrl()),
                     SlShortLink::getOriginalUrl, queryDto.getOriginalUrl())
               .eq(StringUtils.isNotBlank(queryDto.getStatus()),
                   SlShortLink::getStatus, queryDto.getStatus())
               .ge(beginDate != null, SlShortLink::getCreateTime, beginDate)
               .le(endDate != null, SlShortLink::getCreateTime, endDate);

        // 非管理员只能看到自己创建的短链接
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(SlShortLink::getCreateBy, SecurityUtils.getUsername());
        }

        wrapper.orderByDesc(SlShortLink::getCreateTime);

        Page<SlShortLink> result = this.page(queryDto.convetToPage(), wrapper);

        return PageResult.fromPage(result);
    }

    /**
     * 短链接重定向 - 根据短码查找原始URL并增加点击量
     */
    @Override
    public String redirect(String shortCode) {
        SlShortLink link = this.lambdaQuery()
                .eq(SlShortLink::getShortCode, shortCode)
                .one();

        if (link == null) {
            return null;
        }

        // 检查是否已停用
        if ("1".equals(link.getStatus())) {
            return null;
        }

        // 检查是否已过期
        if (link.getExpireTime() != null && link.getExpireTime().before(new Date())) {
            return null;
        }

        // 增加点击量
        this.lambdaUpdate()
                .set(SlShortLink::getClicks, link.getClicks() + 1)
                .eq(SlShortLink::getLinkId, link.getLinkId())
                .update();

        return link.getOriginalUrl();
    }

    /**
     * 获取短链接统计数据
     */
    @Override
    public java.util.Map<String, Object> getLinkStats(Long linkId) {
        SlShortLink link = this.getById(linkId);
        if (link == null) {
            throw new ServiceException("短链接不存在");
        }
        // 非管理员只能查看自己的链接统计
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getUsername().equals(link.getCreateBy())) {
            throw new ServiceException("无权查看该链接统计");
        }
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("linkId", link.getLinkId());
        stats.put("shortCode", link.getShortCode());
        stats.put("originalUrl", link.getOriginalUrl());
        stats.put("title", link.getTitle());
        stats.put("totalClicks", link.getClicks());
        stats.put("status", link.getStatus());
        stats.put("createTime", link.getCreateTime());
        stats.put("expireTime", link.getExpireTime());
        stats.put("createBy", link.getCreateBy());
        // 获取该用户的总链接数和总点击量
        Long userTotalLinks = this.lambdaQuery()
                .eq(SlShortLink::getCreateBy, link.getCreateBy())
                .count();
        Long userTotalClicks = this.lambdaQuery()
                .eq(SlShortLink::getCreateBy, link.getCreateBy())
                .select(SlShortLink::getClicks)
                .list()
                .stream().mapToLong(SlShortLink::getClicks).sum();
        stats.put("userTotalLinks", userTotalLinks);
        stats.put("userTotalClicks", userTotalClicks);
        return stats;
    }

    /**
     * 生成唯一的6位短码
     */
    private String generateShortCode() {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            String code = RandomStringUtils.randomAlphanumeric(6);
            long count = this.lambdaQuery()
                    .eq(SlShortLink::getShortCode, code)
                    .count();
            if (count == 0) {
                return code;
            }
        }
        throw new ServiceException("短码生成失败，请稍后再试");
    }
}
