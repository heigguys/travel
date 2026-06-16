package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码时提交的旧密码、新密码和确认密码。
 */
public record PasswordRequest(@NotBlank String oldPassword, @NotBlank String newPassword, @NotBlank String confirmPassword) {
}
