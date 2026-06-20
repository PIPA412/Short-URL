-- ============================================================
-- 黑名单域名表
-- 用于阻止恶意域名生成短链接
-- ============================================================
DROP TABLE IF EXISTS `sl_blacklist_domain`;
CREATE TABLE `sl_blacklist_domain` (
    `id`          BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `domain`      VARCHAR(255)    NOT NULL                 COMMENT '域名（如 example.com）',
    `create_time` DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_domain` (`domain`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='黑名单域名表';
