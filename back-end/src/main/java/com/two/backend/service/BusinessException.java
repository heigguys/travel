package com.two.backend.service;

/**
 * 业务异常，用于向统一异常处理器传递可展示给前端的错误消息。
 */
public class BusinessException extends RuntimeException {
    /**
     * 创建携带业务提示语的异常。
     *
     * @param message 业务错误消息
     */
    public BusinessException(String message) {
        super(message);
    }
}
