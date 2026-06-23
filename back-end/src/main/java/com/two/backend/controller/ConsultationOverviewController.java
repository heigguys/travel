package com.two.backend.controller;

import com.two.backend.dto.ApiResponse;
import com.two.backend.dto.ConsultationOverviewResponse;
import com.two.backend.service.AuthService;
import com.two.backend.service.ConsultationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationOverviewController {
    private final AuthService authService;
    private final ConsultationService consultationService;

    public ConsultationOverviewController(AuthService authService, ConsultationService consultationService) {
        this.authService = authService;
        this.consultationService = consultationService;
    }

    @GetMapping("/overview")
    public ApiResponse<ConsultationOverviewResponse> overview(HttpSession session) {
        return ApiResponse.ok(consultationService.overview(authService.currentUser(session)));
    }
}
