package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordRequest(@NotBlank String oldPassword, @NotBlank String newPassword) {
}
