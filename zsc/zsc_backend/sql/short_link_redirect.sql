-- ============================================================
-- 短链接重定向服务 - 数据库 DDL
-- ============================================================
-- 说明：
--   1. 此文件定义重构后的短链接表（sl_short_link）和新增的点击日志表（sl_click_log）
--   2. sl_short_link 在原表基础上增加了 user_id、health_status、access_password、max_clicks 字段，
--      并将 clicks 重命名为 click_count
--   3. 如果是对已有数据库执行迁移，请使用下面的 ALTER 语句；
--      如果是全新安装，直接执行 CREATE TABLE 即可
-- ============================================================

-- ============================================================
-- 方式一：全新安装（CREATE TABLE）
-- ============================================================

-- ----------------------------
-- 1. 短链接表
-- ----------------------------
DROP TABLE IF EXISTS `sl_short_link`;
CREATE TABLE `sl_short_link` (
    `link_id`         BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `original_url`    VARCHAR(2048)   NOT NULL                 COMMENT '原始URL',
    `short_code`      VARCHAR(20)     NOT NULL                 COMMENT '短码',
    `user_id`         BIGINT(20)      DEFAULT NULL             COMMENT '创建用户ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `expire_time`     DATETIME        DEFAULT NULL             COMMENT '过期时间',
    `click_count`     BIGINT(20)      DEFAULT 0                COMMENT '点击次数',
    `status`          CHAR(1)         DEFAULT '0'              COMMENT '状态（0-正常 1-过期 2-禁用 3-异常）',
    `health_status`   CHAR(1)         DEFAULT '2'              COMMENT '健康状态（0-健康 1-异常 2-未知）',
    `access_password` VARCHAR(255)    DEFAULT NULL             COMMENT '访问密码（可空）',
    `max_clicks`      BIGINT(20)      DEFAULT NULL             COMMENT '最大点击次数（可空，null 表示无限制）',
    PRIMARY KEY (`link_id`),
    UNIQUE KEY `uk_short_code` (`short_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短链接表';


-- ----------------------------
-- 2. 点击日志表
-- ----------------------------
DROP TABLE IF EXISTS `sl_click_log`;
CREATE TABLE `sl_click_log` (
    `id`              BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `short_link_id`   BIGINT(20)      NOT NULL                 COMMENT '短链接ID',
    `click_time`      DATETIME        NOT NULL                 COMMENT '点击时间',
    `ip`              VARCHAR(64)     DEFAULT NULL             COMMENT '访问IP',
    `user_agent`      VARCHAR(1024)   DEFAULT NULL             COMMENT 'User-Agent',
    `referer`         VARCHAR(1024)   DEFAULT NULL             COMMENT '来源地址',
    `location`        VARCHAR(255)    DEFAULT NULL             COMMENT '地理位置',
    `device_type`     VARCHAR(50)     DEFAULT NULL             COMMENT '设备类型',
    `browser`         VARCHAR(50)     DEFAULT NULL             COMMENT '浏览器',
    PRIMARY KEY (`id`),
    KEY `idx_short_link_id` (`short_link_id`),
    KEY `idx_click_time` (`click_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点击日志表';


-- ============================================================
-- 方式二：已有数据库迁移（ALTER TABLE）
-- 如果 sl_short_link 表已存在，执行以下迁移语句
-- ============================================================

-- /*
-- -- 1. 新增字段
-- ALTER TABLE `sl_short_link`
--     ADD COLUMN `user_id`         BIGINT(20)   DEFAULT NULL AFTER `short_code`,
--     ADD COLUMN `health_status`   CHAR(1)      DEFAULT '2' COMMENT '健康状态（0-健康 1-异常 2-未知）' AFTER `status`,
--     ADD COLUMN `access_password` VARCHAR(255) DEFAULT NULL COMMENT '访问密码' AFTER `health_status`,
--     ADD COLUMN `max_clicks`      BIGINT(20)   DEFAULT NULL COMMENT '最大点击次数' AFTER `access_password`;
--
-- -- 2. 重命名 clicks → click_count（如果原列名还是 clicks）
-- -- ALTER TABLE `sl_short_link` CHANGE COLUMN `clicks` `click_count` BIGINT(20) DEFAULT 0 COMMENT '点击次数';
--
-- -- 3. 补充索引
-- ALTER TABLE `sl_short_link`
--     ADD INDEX `idx_user_id` (`user_id`),
--     ADD INDEX `idx_status` (`status`);
--
-- -- 4. 创建点击日志表
-- CREATE TABLE IF NOT EXISTS `sl_click_log` (
--     `id`              BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
--     `short_link_id`   BIGINT(20)      NOT NULL                 COMMENT '短链接ID',
--     `click_time`      DATETIME        NOT NULL                 COMMENT '点击时间',
--     `ip`              VARCHAR(64)     DEFAULT NULL             COMMENT '访问IP',
--     `user_agent`      VARCHAR(1024)   DEFAULT NULL             COMMENT 'User-Agent',
--     `referer`         VARCHAR(1024)   DEFAULT NULL             COMMENT '来源地址',
--     `location`        VARCHAR(255)    DEFAULT NULL             COMMENT '地理位置',
--     `device_type`     VARCHAR(50)     DEFAULT NULL             COMMENT '设备类型',
--     `browser`         VARCHAR(50)     DEFAULT NULL             COMMENT '浏览器',
--     PRIMARY KEY (`id`),
--     KEY `idx_short_link_id` (`short_link_id`),
--     KEY `idx_click_time` (`click_time`)
-- ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点击日志表';
-- */
