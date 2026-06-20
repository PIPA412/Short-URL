package com.zsc.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsc.module.domain.entity.SlShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 短链接表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-06-16
 */
@Mapper
public interface SlShortLinkMapper extends BaseMapper<SlShortLink> {

    /**
     * 更新点击次数（无需加载整个实体，性能更优）
     *
     * @param id          短链接ID
     * @param clickCount  新点击次数
     * @return 影响行数
     */
    @Update("UPDATE sl_short_link SET click_count = #{clickCount} WHERE link_id = #{id}")
    int updateClickCount(Long id, Long clickCount);
}
