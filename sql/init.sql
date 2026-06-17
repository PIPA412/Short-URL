-- ===================================================================
-- ShortLink Management System - Database Initialization Script
-- ===================================================================
-- Database: MySQL 8.0+
-- Charset: utf8mb4
-- ===================================================================

-- Create database
CREATE DATABASE IF NOT EXISTS `short_link`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `short_link`;

-- ===================================================================
-- Table: sys_user (System User)
-- ===================================================================
DROP TABLE IF EXISTS `short_link_log`;
DROP TABLE IF EXISTS `short_link`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `username`    VARCHAR(50)  NOT NULL COMMENT 'Username',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt hashed password',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT 'Display name',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT 'Email address',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    `status`      TINYINT      DEFAULT 0 COMMENT 'Status: 0=OK, 1=DISABLED',
    `login_ip`    VARCHAR(50)  DEFAULT NULL COMMENT 'Last login IP',
    `login_date`  DATETIME     DEFAULT NULL COMMENT 'Last login time',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `del_flag`    TINYINT      DEFAULT 0 COMMENT 'Logical delete flag: 0=normal, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System user table';

-- ===================================================================
-- Table: short_link (Short Link)
-- ===================================================================
CREATE TABLE `short_link` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id`      BIGINT        NOT NULL COMMENT 'Owner user ID',
    `short_code`   VARCHAR(16)   NOT NULL COMMENT 'Short code (Base62 encoded)',
    `original_url` VARCHAR(2048) NOT NULL COMMENT 'Original long URL',
    `title`        VARCHAR(200)  DEFAULT NULL COMMENT 'Link title',
    `description`  VARCHAR(500)  DEFAULT NULL COMMENT 'Link description',
    `expire_time`  DATETIME      DEFAULT NULL COMMENT 'Expiration time (NULL = never expires)',
    `click_count`  BIGINT        DEFAULT 0 COMMENT 'Total click count',
    `status`       TINYINT       DEFAULT 0 COMMENT 'Status: 0=NORMAL, 1=EXPIRED, 2=DISABLED',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time`  DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `del_flag`     TINYINT       DEFAULT 0 COMMENT 'Logical delete flag: 0=normal, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_short_code` (`short_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_short_link_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Short link table';

-- ===================================================================
-- Table: short_link_log (Click Access Log)
-- ===================================================================
CREATE TABLE `short_link_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `link_id`     BIGINT       NOT NULL COMMENT 'Short link ID',
    `short_code`  VARCHAR(16)  NOT NULL COMMENT 'Short code (redundant for fast query)',
    `access_ip`   VARCHAR(50)  DEFAULT NULL COMMENT 'Visitor IP address',
    `user_agent`  VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent header',
    `referer`     VARCHAR(500) DEFAULT NULL COMMENT 'HTTP Referer header',
    `country`     VARCHAR(50)  DEFAULT NULL COMMENT 'Geolocation: country',
    `city`        VARCHAR(50)  DEFAULT NULL COMMENT 'Geolocation: city',
    `device_type` VARCHAR(20)  DEFAULT NULL COMMENT 'Device type: PC, MOBILE, TABLET',
    `browser`     VARCHAR(50)  DEFAULT NULL COMMENT 'Browser name',
    `os`          VARCHAR(50)  DEFAULT NULL COMMENT 'Operating system',
    `access_time` DATETIME     NOT NULL COMMENT 'Access timestamp',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    PRIMARY KEY (`id`),
    KEY `idx_link_id` (`link_id`),
    KEY `idx_short_code` (`short_code`),
    KEY `idx_access_time` (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Short link access log table';

-- ===================================================================
-- Seed Data: Default admin user (password: admin123, BCrypt encoded)
-- ===================================================================
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`, `create_time`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'Administrator', 'admin@shortlink.com', 0, NOW());
