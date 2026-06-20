-- ============================================================
-- 管理员专用菜单与权限
-- role_id=1 (管理员) 额外拥有用户管理、系统配置、全系统统计
-- role_id=2 (普通用户) 仅拥有短链接管理
-- ============================================================

-- 添加「系统管理」目录 (仅管理员可见)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('系统管理', 0, 2, 'system', NULL, 1, 0, 'M', '0', '0', '', 'system', 'admin', sysdate(), '管理员 - 系统管理目录');

-- 获取刚插入的系统管理菜单ID
SET @admin_menu_id = LAST_INSERT_ID();

-- 用户管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('用户管理', @admin_menu_id, 1, 'user', 'admin/user/index', 1, 0, 'C', '0', '0', 'admin:user:list', 'user', 'admin', sysdate(), '管理员 - 用户管理');

SET @user_menu_id = LAST_INSERT_ID();

-- 用户管理按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, is_frame, is_cache, menu_type, visible, status, perms, create_by, create_time, remark)
VALUES
('用户查询', @user_menu_id, 1, '#', 1, 0, 'F', '0', '0', 'admin:user:query', 'admin', sysdate(), ''),
('用户新增', @user_menu_id, 2, '#', 1, 0, 'F', '0', '0', 'admin:user:add', 'admin', sysdate(), ''),
('用户修改', @user_menu_id, 3, '#', 1, 0, 'F', '0', '0', 'admin:user:edit', 'admin', sysdate(), ''),
('用户删除', @user_menu_id, 4, '#', 1, 0, 'F', '0', '0', 'admin:user:remove', 'admin', sysdate(), ''),
('用户重置密码', @user_menu_id, 5, '#', 1, 0, 'F', '0', '0', 'admin:user:resetPwd', 'admin', sysdate(), '');

-- 参数设置菜单（短链接系统配置）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('参数设置', @admin_menu_id, 2, 'config', 'admin/config/index', 1, 0, 'C', '0', '0', 'admin:config:list', 'edit', 'admin', sysdate(), '管理员 - 系统配置');

SET @config_menu_id = LAST_INSERT_ID();

-- 参数设置按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, is_frame, is_cache, menu_type, visible, status, perms, create_by, create_time, remark)
VALUES
('配置查询', @config_menu_id, 1, '#', 1, 0, 'F', '0', '0', 'admin:config:query', 'admin', sysdate(), ''),
('配置新增', @config_menu_id, 2, '#', 1, 0, 'F', '0', '0', 'admin:config:add', 'admin', sysdate(), ''),
('配置修改', @config_menu_id, 3, '#', 1, 0, 'F', '0', '0', 'admin:config:edit', 'admin', sysdate(), ''),
('配置删除', @config_menu_id, 4, '#', 1, 0, 'F', '0', '0', 'admin:config:remove', 'admin', sysdate(), '');

-- ============================================================
-- 为管理员(role_id=1)分配新菜单权限
-- ============================================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'admin:%' OR (menu_name IN ('系统管理') AND menu_type = 'M' AND parent_id = 0 AND menu_id >= @admin_menu_id);

-- 验证
SELECT '=== 管理员菜单 ===' AS info;
SELECT m.menu_id, m.menu_name, m.menu_type, m.perms
FROM sys_menu m
JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
WHERE rm.role_id = 1
ORDER BY m.menu_id;

SELECT '=== 普通用户菜单 ===' AS info;
SELECT m.menu_id, m.menu_name, m.menu_type, m.perms
FROM sys_menu m
JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
WHERE rm.role_id = 2
ORDER BY m.menu_id;
