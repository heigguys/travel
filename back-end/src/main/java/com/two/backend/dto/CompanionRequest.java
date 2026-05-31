package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanionRequest(@NotBlank String name, @NotBlank String gender, @NotBlank String idCard, Boolean bedNeeded) {
}
