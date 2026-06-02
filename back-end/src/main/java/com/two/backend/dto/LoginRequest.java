package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录时提交的员工编号和密码。
 */
public record LoginRequest(@NotBlank String employeeNo, @NotBlank String password) {
}
