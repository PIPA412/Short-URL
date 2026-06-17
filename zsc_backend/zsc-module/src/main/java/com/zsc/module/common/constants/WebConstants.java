package com.zsc.module.common.constants;

public class WebConstants {
    // 角色ID（注意与数据库保持一致，如后续变更请同步）
    public static final long ROLE_SUPER_ADMIN = 1;
    public static final long ROLE_ADMIN = 2;
    public static final long ROLE_READER = 3;

    // 状态
    public static final int STATUS_ACTIVE = 1;     // 激活
    public static final int STATUS_INACTIVE = 0;   // 停用

    // 默认密码等
    public static final String DEFAULT_PASSWORD = "123456";

    // 分页参数
     public static final int DEFAULT_PAGE_SIZE = 10;

    // 借阅数量限制（如不与ReaderType枚举绑定，可独立作为常量）
    public static final int STUDENT_MAX_BORROW = 10;
    public static final int STAFF_MAX_BORROW = 20;


    // 系统信息
    public static final String SYSTEM_NAME = "图书借阅管理系统";
    public static final String COPYRIGHT = "Copyright © 2025 某某公司";


}

