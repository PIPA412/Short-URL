-- ============================================================
-- 短链接管理系统 - 菜单精简
-- 直接删除 系统管理/系统监控/系统工具 及其所有子功能
-- 执行前提：已执行 zsc.sql（原始若依数据）和 short_link.sql（短链接业务表）
-- ============================================================

-- ============================================================
-- 第一步：清理角色-菜单关联（必须先删关联，再删菜单）
-- ============================================================

-- 系统管理(1)及其所有子菜单
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (1, 100, 101, 102, 103, 104, 105, 106, 107, 108, 500, 501);
DELETE FROM `sys_role_menu` WHERE `menu_id` BETWEEN 1000 AND 1060;

-- 系统监控(2)及其所有子菜单
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (2, 109, 110, 111, 112, 113, 114);

-- 系统工具(3)及其所有子菜单
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (3, 115, 116, 117);

-- ============================================================
-- 第二步：删除菜单（从叶子节点往根节点删，先删子再删父）
-- ============================================================

-- 删除所有操作按钮（F类型）
DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 1000 AND 1060;

-- 删除系统管理下的子菜单
DELETE FROM `sys_menu` WHERE `menu_id` IN (100, 101, 102, 103, 104, 105, 106, 107, 108, 500, 501);

-- 删除系统监控下的子菜单
DELETE FROM `sys_menu` WHERE `menu_id` IN (109, 110, 111, 112, 113, 114);

-- 删除系统工具下的子菜单
DELETE FROM `sys_menu` WHERE `menu_id` IN (115, 116, 117);

-- 删除三个顶级目录
DELETE FROM `sys_menu` WHERE `menu_id` IN (1, 2, 3);

-- ============================================================
-- 第三步：添加短链接系统配置项
-- ============================================================

INSERT IGNORE INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
VALUES
('短码默认长度', 'shortlink.code.length', '6', 'Y', 'admin', sysdate(), '生成短码的默认字符长度（新链接生效）'),
('短码字符集', 'shortlink.code.charset', 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789', 'Y', 'admin', sysdate(), '短码允许使用的字符集（新链接生效）'),
('允许自定义短码', 'shortlink.allow.custom.code', 'true', 'Y', 'admin', sysdate(), '是否允许用户自定义短码（true/false）'),
('默认有效期(天)', 'shortlink.default.expire.days', '0', 'Y', 'admin', sysdate(), '短链接默认有效期天数（0表示永久有效）'),
('黑名单域名', 'shortlink.blacklist.domains', '', 'Y', 'admin', sysdate(), '禁止生成短链接的域名列表，多个用逗号分隔'),
('每小时生成上限', 'shortlink.rate.limit.per.hour', '50', 'Y', 'admin', sysdate(), '每个用户每小时生成短链接的数量上限');

-- ============================================================
-- 第四步：确保管理员拥有业务管理权限
-- ============================================================

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu` WHERE `perms` LIKE 'biz:shortlink:%';

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES (1, 200);
