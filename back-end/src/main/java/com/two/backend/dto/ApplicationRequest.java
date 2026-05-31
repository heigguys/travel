package com.two.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(@NotNull @Min(1) Integer applicantCount, String optionText) {
}
