package com.shortlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    @Update("UPDATE short_link SET click_count = click_count + 1 WHERE id = #{id}")
    int incrementClickCount(@Param("id") Long id);
}
