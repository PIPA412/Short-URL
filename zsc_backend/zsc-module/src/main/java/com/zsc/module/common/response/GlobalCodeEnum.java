package com.zsc.module.common.response;


import lombok.Getter;

/**
 * description: 全局响应码枚举型
 */
@Getter
public enum  GlobalCodeEnum {
    /**
     * 全局响应码的定义
     */
    SUCCESS_000(200,"success"),
    FAIL_9996(996, "HTTP请求方法错误"),
    FAIL_9997(997, "参数校验错误"),
    FAIL_9998(998, "参数类型转换错误"),
    CODE_FAIL(399, "校验码校验失败"),

    INTERNAL_SERVER_ERROR(500, "服务器内部异常"),
    FORBIDDEN(403, "没有相应权限，无法访问"),
    UNAUTHORIZED(401, "登录或登录已过期"),
    SERVICE_ERROR(503, "系统服务层异常"),
    LOGIN_ERROR(1199, "用户名或密码错误"),
    USER_DISABLED(1299, "用户已被禁用"),
    AUTH_ERROR(1399, "认证失败"),
    DATA_DUPLICATE(1999, "数据重复");
    /**
     * 编码
     */
    private Integer code;

    /**
     * 描述
     */
    private String description;

    GlobalCodeEnum(Integer code, String description) {
        this.code =code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举类型的方法
     */
    public  static GlobalCodeEnum getByCode(Integer code){
        //判断编码是否为空
        if(code == null){
            return null;
        }
        //遍历枚举类
        GlobalCodeEnum[] values =  GlobalCodeEnum.values();
        for (GlobalCodeEnum value : values) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}

