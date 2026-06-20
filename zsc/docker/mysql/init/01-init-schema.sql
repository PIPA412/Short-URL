-- ============================================================
-- Docker MySQL 初始化脚本
-- 当 MySQL 容器首次启动时自动执行
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `zsc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `zsc`;

-- 注意：以下表结构需要通过其他方式初始化
-- 方式一：将 zsc.sql 和 short_link_redirect.sql 也放入本目录，按文件名顺序执行
-- 方式二：启动后手动执行 docker exec -i mysql mysql zsc < zsc.sql
-- 方式三：通过 Flyway / Liquibase 管理

-- 此处仅做数据库创建，具体表结构由应用启动时确保已存在
