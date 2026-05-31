package com.two.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultationRequest(@NotBlank String content) {
}
