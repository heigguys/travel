package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 保存申请随行人员信息时的单条请求数据。
 */
public record CompanionRequest(
        @NotBlank
        @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z·\\s]{2,20}$")
        String name,
        @NotBlank String gender,
        @NotBlank
        @Pattern(regexp = "^\\d{17}[\\dXx]$")
        String idCard,
        Boolean bedNeeded
) {
}
