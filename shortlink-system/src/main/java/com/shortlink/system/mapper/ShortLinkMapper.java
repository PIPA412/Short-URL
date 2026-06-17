package com.shortlink.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.system.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * ShortLink mapper — extends MyBatis-Plus BaseMapper for standard CRUD.
 *
 * @author ShortLink
 */
@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    /**
     * Paginated query of short links owned by a user, with optional filters.
     */
    IPage<ShortLink> selectPageByUserId(Page<ShortLink> page,
                                         @Param("userId") Long userId,
                                         @Param("title") String title,
                                         @Param("status") Integer status);

    /**
     * Get click statistics for a link (grouped by day).
     */
    List<Map<String, Object>> countByDay(@Param("linkId") Long linkId,
                                          @Param("days") Integer days);
}
