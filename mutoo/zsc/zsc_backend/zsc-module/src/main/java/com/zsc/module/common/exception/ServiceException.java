package com.zsc.module.common.exception;

/**
 * 业务异常类
 * 
 * @author zsc
 */
public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * 错误消息
     */
    private String message;

    public ServiceException(String message) {
        super(message);
        this.message = message;
    }

    public ServiceException() {
        super();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
