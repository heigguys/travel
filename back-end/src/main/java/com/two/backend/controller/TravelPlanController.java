package com.two.backend.controller;

import com.two.backend.dto.ApiResponse;
import com.two.backend.dto.PlanRequest;
import com.two.backend.model.Application;
import com.two.backend.model.TravelPlan;
import com.two.backend.model.User;
import com.two.backend.service.AuthService;
import com.two.backend.service.TravelPlanService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/plans")
public class TravelPlanController {
    private final AuthService authService;
    private final TravelPlanService travelPlanService;

    public TravelPlanController(AuthService authService, TravelPlanService travelPlanService) {
        this.authService = authService;
        this.travelPlanService = travelPlanService;
    }

    @GetMapping
    public ApiResponse<List<TravelPlan>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String sort,
                                              HttpSession session) {
        return ApiResponse.ok(travelPlanService.list(authService.currentUser(session), keyword, status, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<TravelPlan> detail(@PathVariable Long id, HttpSession session) {
        return ApiResponse.ok(travelPlanService.getVisible(id, authService.currentUser(session)));
    }

    @PostMapping
    public ApiResponse<TravelPlan> create(PlanForm form, HttpSession session) throws IOException {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        return ApiResponse.ok(travelPlanService.create(form.request(), form.file()));
    }

    @PostMapping("/{id}")
    public ApiResponse<TravelPlan> update(@PathVariable Long id, PlanForm form, HttpSession session) throws IOException {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        return ApiResponse.ok(travelPlanService.update(id, form.request(), form.file()));
    }

    @GetMapping("/{id}/delete-preview")
    public ApiResponse<List<Application>> deletePreview(@PathVariable Long id, HttpSession session) {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        return ApiResponse.ok(travelPlanService.deletePreview(id));
    }

    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        travelPlanService.delete(id);
        return ApiResponse.message("旅行计划已删除");
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable Long id, HttpSession session) {
        Resource resource = travelPlanService.file(id, authService.currentUser(session));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(resource.getFilename()).build().toString())
                .body(resource);
    }

    public record PlanForm(
            String destination,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            BigDecimal price,
            Integer capacity,
            Boolean published,
            MultipartFile file
    ) {
        PlanRequest request() {
            return new PlanRequest(destination, startDate, endDate, price, capacity, published);
        }
    }
}
