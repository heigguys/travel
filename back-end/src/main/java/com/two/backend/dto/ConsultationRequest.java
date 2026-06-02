package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 发送旅行计划咨询消息时的请求体。
 */
public record ConsultationRequest(@NotBlank String content) {
}
