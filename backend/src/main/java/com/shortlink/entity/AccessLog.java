package com.shortlink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("access_log")
public class AccessLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shortLinkId;
    private String ip;
    private String userAgent;
    private String referer;
    private LocalDateTime accessTime;
}
