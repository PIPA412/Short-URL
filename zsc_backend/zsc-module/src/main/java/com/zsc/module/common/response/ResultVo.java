package com.zsc.module.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * description: 统一封装后台响应接口,推荐使用泛型进行数据封装
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ResultVo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /*返回的响应编码 */
    private Integer code;

    /*返回的响应消息*/
    private String message;

    /*返回业务数据对象,如果数据为null，不进行序列化*/
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    //返回成功的消息,不带数据
    public static ResultVo<String> ok() {
        return new ResultVo(GlobalCodeEnum.SUCCESS_000, null);
    }

    // 返回带数据成功响应
    public static <T> ResultVo<T> ok(T data) {
        return new ResultVo(GlobalCodeEnum.SUCCESS_000, data);
    }

    //返回失败的消息,不带数据
    public static ResultVo<String> fail() {
        return new ResultVo(GlobalCodeEnum.INTERNAL_SERVER_ERROR, null);
    }

    // 返回带数据失败的消息
    public static <T> ResultVo<T> fail(T data) {
        return new ResultVo(GlobalCodeEnum.INTERNAL_SERVER_ERROR, data);
    }


    public static <T> ResultVo<T> build(GlobalCodeEnum globalCodeEnum,  T data) {
        return new ResultVo(globalCodeEnum, data);
    }

    public static <T> ResultVo<T> build(GlobalCodeEnum globalCodeEnum) {
        return new ResultVo(globalCodeEnum,null);
    }
    // 构造函数
    public ResultVo(GlobalCodeEnum globalCodeEnum, T data) {
        this.code = globalCodeEnum.getCode();
        this.message = globalCodeEnum.getDescription();
        this.data = data;
    }

    /**
     * 返回系统指定的异常错误
     */
    public static <T> ResultVo<T> systemException(GlobalCodeEnum globalCodeEnum, T data) {
        return new ResultVo<>(globalCodeEnum, data);
    }
}

