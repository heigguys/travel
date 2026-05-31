package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String employeeNo, @NotBlank String password) {
}
