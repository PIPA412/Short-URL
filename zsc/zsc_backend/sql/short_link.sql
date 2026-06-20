-- ----------------------------
-- 短链接管理平台 - 数据库初始化
-- （如果重新执行，整个文件可重复运行）
-- ----------------------------

-- ============================================================
-- 第一步：清理旧数据（防止残留导致重复菜单）
-- ============================================================
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (
    SELECT `menu_id` FROM (SELECT `menu_id` FROM `sys_menu` WHERE `perms` LIKE 'biz:shortlink:%' OR (`menu_name` = '短链接管理' AND `menu_type` = 'C')) AS tmp
);
DELETE FROM `sys_menu` WHERE `perms` LIKE 'biz:shortlink:%' OR (`menu_name` = '短链接管理' AND `menu_type` = 'C');
DELETE FROM `sys_menu` WHERE `menu_name` = '业务管理' AND `menu_type` = 'M' AND `parent_id` = 0;

-- ============================================================
-- 第二步：创建短链接表
-- ============================================================
DROP TABLE IF EXISTS `sl_short_link`;
CREATE TABLE `sl_short_link` (
    `link_id`       BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '链接ID',
    `short_code`    VARCHAR(20)     NOT NULL                 COMMENT '短码',
    `original_url`  VARCHAR(2048)   NOT NULL                 COMMENT '原始URL',
    `title`         VARCHAR(128)    DEFAULT NULL             COMMENT '标题',
    `clicks`        BIGINT(20)      DEFAULT 0                COMMENT '点击次数',
    `status`        CHAR(1)         DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    `expire_time`   DATETIME        DEFAULT NULL             COMMENT '过期时间',
    `create_by`     VARCHAR(64)     DEFAULT ''               COMMENT '创建者',
    `create_time`   DATETIME        DEFAULT NULL             COMMENT '创建时间',
    `update_by`     VARCHAR(64)     DEFAULT ''               COMMENT '更新者',
    `update_time`   DATETIME        DEFAULT NULL             COMMENT '更新时间',
    `remark`        VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    PRIMARY KEY (`link_id`),
    UNIQUE KEY `uk_short_code` (`short_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短链接表';

-- ============================================================
-- 第三步：菜单与权限
-- ============================================================

-- 3a. 添加「业务管理」目录（排在首页下方第一位，order_num = 1）
-- 注意：菜单排序和冗余菜单隐藏由 menu_restructure.sql 统一处理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (200, '业务管理', 0, 1, 'biz', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), '', NULL, '业务管理目录')
ON DUPLICATE KEY UPDATE `order_num` = 1;

-- 3b. 添加「短链接管理」菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('短链接管理', 200, 1, 'shortlink', 'biz/shortLink/index', NULL, NULL, 1, 0, 'C', '0', '0', 'biz:shortlink:list', 'link', 'admin', sysdate(), '', NULL, '短链接管理菜单');

-- 3c. 添加操作按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '短链接查询', menu_id, 1, '#', NULL, 1, 0, 'F', '0', '0', 'biz:shortlink:query', '#', 'admin', sysdate(), '', NULL, ''
FROM `sys_menu` WHERE `perms` = 'biz:shortlink:list' AND `menu_type` = 'C' AND `menu_name` = '短链接管理';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '短链接新增', menu_id, 2, '#', NULL, 1, 0, 'F', '0', '0', 'biz:shortlink:add', '#', 'admin', sysdate(), '', NULL, ''
FROM `sys_menu` WHERE `perms` = 'biz:shortlink:list' AND `menu_type` = 'C' AND `menu_name` = '短链接管理';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '短链接修改', menu_id, 3, '#', NULL, 1, 0, 'F', '0', '0', 'biz:shortlink:edit', '#', 'admin', sysdate(), '', NULL, ''
FROM `sys_menu` WHERE `perms` = 'biz:shortlink:list' AND `menu_type` = 'C' AND `menu_name` = '短链接管理';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '短链接删除', menu_id, 4, '#', NULL, 1, 0, 'F', '0', '0', 'biz:shortlink:remove', '#', 'admin', sysdate(), '', NULL, ''
FROM `sys_menu` WHERE `perms` = 'biz:shortlink:list' AND `menu_type` = 'C' AND `menu_name` = '短链接管理';

-- 3d. 为管理员角色分配权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` WHERE `perms` LIKE 'biz:shortlink:%';
