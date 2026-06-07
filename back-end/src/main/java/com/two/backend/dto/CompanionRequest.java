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
        @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$")
        String idCard,
        Boolean bedNeeded
) {
}
