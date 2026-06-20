-- ============================================================
-- 安全与风控模块 - 配置预置数据
-- 将配置项插入 sys_config 表
-- ============================================================

-- 频率限制 - 每用户每小时生成短链接上限（默认 50）
INSERT IGNORE INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
VALUES ('短链接-用户每小时生成上限', 'sys.rate.shortlink.user.hourly', '50', 'Y', 'admin', NOW(), '每用户每小时最多生成的短链接数，超出触发频率限制');

-- 频率限制 - 每 IP 每小时生成短链接上限（默认 100）
INSERT IGNORE INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
VALUES ('短链接-IP每小时生成上限', 'sys.rate.shortlink.ip.hourly', '100', 'Y', 'admin', NOW(), '每 IP 每小时最多生成的短链接数，超出触发频率限制');
