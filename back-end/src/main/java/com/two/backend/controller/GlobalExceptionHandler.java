package com.two.backend.controller;

import com.two.backend.dto.ApiResponse;
import com.two.backend.service.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * 全局异常处理器，把业务异常、参数异常和未知异常统一转换为 ApiResponse。
 */
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * 处理业务异常，直接将业务提示返回给前端。
     *
     * @param exception 业务异常
     * @return 失败响应
     */
    public ApiResponse<Void> handleBusiness(BusinessException exception) {
        return ApiResponse.fail(exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * 处理 Bean Validation 参数校验异常。
     *
     * @param exception 参数异常
     * @return 参数错误响应
     */
    public ApiResponse<Void> handleValidation(Exception exception) {
        return ApiResponse.fail("输入内容不完整或格式不正确");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    /**
     * 处理未预期异常，避免后端堆栈直接暴露给前端。
     *
     * @param exception 未预期异常
     * @return 服务端错误响应
     */
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.fail("服务器处理失败：" + exception.getMessage());
    }
}
