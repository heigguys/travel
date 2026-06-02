package com.two.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 管理员新增或编辑旅行计划时的核心表单数据。
 */
public record PlanRequest(
        @NotBlank String destination,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull @Min(1) Integer capacity,
        Boolean published
) {
}
