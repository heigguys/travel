package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 保存申请随行人员信息时的单条请求数据。
 */
public record CompanionRequest(@NotBlank String name, @NotBlank String gender, @NotBlank String idCard, Boolean bedNeeded) {
}
