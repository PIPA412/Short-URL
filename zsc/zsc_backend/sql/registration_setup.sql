-- ============================================================
-- 注册功能与角色权限设置
-- ============================================================

-- 1. 开启注册功能
UPDATE sys_config SET config_value = 'true' WHERE config_key = 'sys.account.registerUser';

-- 2. 为普通角色(role_id=2)分配菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 200);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'biz:shortlink:%';

-- 3. 验证
SELECT '=== 注册开关 ===' AS info;
SELECT config_key, config_value FROM sys_config WHERE config_key LIKE '%register%';

SELECT '=== 角色菜单权限 ===' AS info;
SELECT r.role_id, r.role_name, COUNT(rm.menu_id) AS menu_count
FROM sys_role r LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
GROUP BY r.role_id, r.role_name;
