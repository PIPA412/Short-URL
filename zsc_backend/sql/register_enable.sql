-- ----------------------------
-- 开启注册功能 + 注册用户默认角色 + 权限分配
-- ----------------------------

-- 开启注册功能
UPDATE `sys_config` SET `config_value` = 'true' WHERE `config_key` = 'sys.account.registerUser';

-- 确认普通角色存在（若不存在则创建，用于新注册用户默认角色）
INSERT IGNORE INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', sysdate(), '', NULL, '普通角色');

-- 为普通角色分配「业务管理」目录菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, menu_id FROM `sys_menu` WHERE `menu_name` = '业务管理' AND `menu_type` = 'M' AND `parent_id` = 0;

-- 为普通角色分配短链接管理的所有子菜单权限（查询、统计、新增、修改、删除）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, menu_id FROM `sys_menu` WHERE `perms` LIKE 'biz:shortlink:%';
