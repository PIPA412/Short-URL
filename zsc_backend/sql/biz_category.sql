DROP TABLE IF EXISTS biz_category;
CREATE TABLE biz_category (
                              category_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '类别ID',
                              category_name varchar(100) NOT NULL COMMENT '类别名称',
                              sort_order int(11) DEFAULT '0' COMMENT '排序',
                              status char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
                              create_time datetime DEFAULT NULL COMMENT '创建时间',
                              update_time datetime DEFAULT NULL COMMENT '更新时间',
                              PRIMARY KEY (category_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='业务类别表';