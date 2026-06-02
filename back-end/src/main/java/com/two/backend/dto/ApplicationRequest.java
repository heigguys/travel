package com.two.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 员工提交或更新旅行申请时的请求体。
 */
public record ApplicationRequest(@NotNull @Min(1) Integer applicantCount, String optionText) {
}
