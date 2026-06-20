#!/bin/bash
# ============================================================
# MySQL Docker 初始化脚本
# MYSQL_DATABASE 环境变量已由 docker-compose 指定为 zsc，
# 数据库容器会自动创建。此脚本将项目 SQL 文件导入 zsc 库。
#
# 在初始化阶段，MySQL 允许通过 unix socket 无密码连接，
# 所以不需要 -p 参数。
# ============================================================

set -e

echo ">>> [Docker Init] 开始导入 SQL 文件到 zsc 数据库..."

MYSQL_CMD="mysql -uroot"
SQL_DIR="/sql-init"

if [ -d "${SQL_DIR}" ]; then
    # 按顺序导入 SQL 文件
    SQL_FILES=(
        "zsc.sql"
        "biz_category.sql"
        "short_link_redirect.sql"
        "sl_blacklist_domain.sql"
        "rate_limit_config.sql"
        "admin_menus.sql"
        "menu_restructure.sql"
        "registration_setup.sql"
    )

    for f in "${SQL_FILES[@]}"; do
        file_path="${SQL_DIR}/${f}"
        if [ -f "$file_path" ]; then
            echo ">>> [Docker Init] 导入 $(basename $f) ..."
            ${MYSQL_CMD} zsc < "$file_path"
        fi
    done
fi

echo ">>> [Docker Init] 数据库初始化完成！"
