package com.two.backend.controller;

import com.two.backend.dto.ApiResponse;
import com.two.backend.dto.ConsultationRequest;
import com.two.backend.model.Consultation;
import com.two.backend.service.AuthService;
import com.two.backend.service.ConsultationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans/{planId}/consultations")
public class ConsultationController {
    private final AuthService authService;
    private final ConsultationService consultationService;

    public ConsultationController(AuthService authService, ConsultationService consultationService) {
        this.authService = authService;
        this.consultationService = consultationService;
    }

    @GetMapping
    public ApiResponse<List<Consultation>> list(@PathVariable Long planId, HttpSession session) {
        return ApiResponse.ok(consultationService.list(planId, authService.currentUser(session)));
    }

    @PostMapping
    public ApiResponse<Consultation> send(@PathVariable Long planId, @Valid @RequestBody ConsultationRequest request, HttpSession session) {
        return ApiResponse.ok(consultationService.send(planId, authService.currentUser(session), request));
    }

    @PostMapping("/close")
    public ApiResponse<Void> close(@PathVariable Long planId, HttpSession session) {
        consultationService.close(planId, authService.currentUser(session));
        return ApiResponse.message("对话已结束");
    }
}
