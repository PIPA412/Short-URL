package com.zsc.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsc.module.domain.entity.SlClickLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * <p>
 * 短链接点击日志表 Mapper 接口
 * </p>
 *
 * @author zsc
 * @since 2026-06-17
 */
@Mapper
public interface SlClickLogMapper extends BaseMapper<SlClickLog> {

    /**
     * 记录或更新每日点击量（使用 ON DUPLICATE KEY UPDATE 避免并发问题）
     */
    void incrementDailyClick(@Param("linkId") Long linkId, @Param("clickDate") LocalDate clickDate);
}
