-- ----------------------------
-- 短链接点击统计 - 数据库初始化
-- （如果重新执行，整个文件可重复运行）
-- ----------------------------

-- ============================================================
-- 第一步：创建点击日志表
-- ============================================================
DROP TABLE IF EXISTS `sl_click_log`;
CREATE TABLE `sl_click_log` (
    `log_id`        BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    `link_id`       BIGINT(20)      NOT NULL                 COMMENT '链接ID',
    `click_date`    DATE            NOT NULL                 COMMENT '点击日期',
    `click_count`   BIGINT(20)      DEFAULT 0                COMMENT '点击次数',
    PRIMARY KEY (`log_id`),
    UNIQUE KEY `uk_link_date` (`link_id`, `click_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短链接点击日志表';

-- ============================================================
-- 第二步：添加点击统计菜单（挂在短链接管理下面）
-- ============================================================

-- 获取短链接管理菜单ID，并插入统计子菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '点击统计', menu_id, 5, 'stats/:linkId(\\\d+)', 'biz/shortLink/stats', NULL, NULL, 1, 0, 'C', '1', '0', 'biz:shortlink:stats', 'chart', 'admin', sysdate(), '', NULL, '短链接点击统计页面'
FROM `sys_menu` WHERE `perms` = 'biz:shortlink:list' AND `menu_type` = 'C' AND `menu_name` = '短链接管理';

-- 为管理员角色分配权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` WHERE `perms` = 'biz:shortlink:stats';
