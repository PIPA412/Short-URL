package com.zsc.module.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * <p>
 * 短链接点击日志表
 * </p>
 *
 * @author zsc
 * @since 2026-06-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sl_click_log")
public class SlClickLog {

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 链接ID
     */
    private Long linkId;

    /**
     * 点击日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate clickDate;

    /**
     * 点击次数
     */
    private Long clickCount;
}
