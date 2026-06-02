package com.two.backend.dto;

/**
 * 后端统一 JSON 响应包装，承载成功状态、提示消息和业务数据。
 */
public record ApiResponse<T>(boolean success, String message, T data) {
    /**
     * 构造成功且带数据的响应。
     *
     * @param data 响应数据
     * @return 统一响应对象
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "ok", data);
    }

    /**
     * 构造成功但仅返回提示消息的响应。
     *
     * @param message 成功提示
     * @return 统一响应对象
     */
    public static <T> ApiResponse<T> message(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * 构造失败响应。
     *
     * @param message 失败原因
     * @return 统一响应对象
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
