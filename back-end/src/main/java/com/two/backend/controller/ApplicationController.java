package com.two.backend.controller;

import com.two.backend.dto.ApiResponse;
import com.two.backend.dto.ApplicationRequest;
import com.two.backend.dto.CompanionRequest;
import com.two.backend.model.Application;
import com.two.backend.model.Companion;
import com.two.backend.model.User;
import com.two.backend.service.ApplicationService;
import com.two.backend.service.AuthService;
import com.two.backend.service.PdfExportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApplicationController {
    private final AuthService authService;
    private final ApplicationService applicationService;
    private final PdfExportService pdfExportService;

    public ApplicationController(AuthService authService, ApplicationService applicationService, PdfExportService pdfExportService) {
        this.authService = authService;
        this.applicationService = applicationService;
        this.pdfExportService = pdfExportService;
    }

    @PostMapping("/plans/{planId}/apply")
    public ApiResponse<Application> apply(@PathVariable Long planId, @Valid @RequestBody ApplicationRequest request, HttpSession session) {
        return ApiResponse.ok(applicationService.apply(planId, authService.currentUser(session), request));
    }

    @PostMapping("/applications/{applicationId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long applicationId, HttpSession session) {
        applicationService.cancel(applicationId, authService.currentUser(session));
        return ApiResponse.message("申请已取消");
    }

    @GetMapping("/applications/{applicationId}/companions")
    public ApiResponse<List<Companion>> companions(@PathVariable Long applicationId, HttpSession session) {
        return ApiResponse.ok(applicationService.companions(applicationId, authService.currentUser(session)));
    }

    @PostMapping("/applications/{applicationId}/companions")
    public ApiResponse<List<Companion>> saveCompanions(@PathVariable Long applicationId,
                                                       @Valid @RequestBody List<CompanionRequest> requests,
                                                       HttpSession session) {
        return ApiResponse.ok(applicationService.replaceCompanions(applicationId, authService.currentUser(session), requests));
    }

    @GetMapping("/my-applications")
    public ApiResponse<List<Application>> myApplications(HttpSession session) {
        return ApiResponse.ok(applicationService.myApplications(authService.currentUser(session)));
    }

    @GetMapping("/my-applications/export.pdf")
    public ResponseEntity<byte[]> export(HttpSession session) {
        User user = authService.currentUser(session);
        byte[] pdf = pdfExportService.exportApplications(user, applicationService.myApplications(user));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("my-applications.pdf").build().toString())
                .body(pdf);
    }
}
