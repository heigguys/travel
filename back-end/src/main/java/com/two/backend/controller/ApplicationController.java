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
/**
 * 旅行申请控制器，负责申请、取消、随行人维护和申请 PDF 导出。
 */
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
    /**
     * 当前用户申请指定旅行计划，重复申请会更新原 ACTIVE 记录。
     *
     * @param planId 旅行计划 ID
     * @param request 申请人数和备注
     * @param session 当前 HTTP 会话
     * @return 保存后的申请记录
     */
    public ApiResponse<Application> apply(@PathVariable Long planId, @Valid @RequestBody ApplicationRequest request, HttpSession session) {
        return ApiResponse.ok(applicationService.apply(planId, authService.currentUser(session), request));
    }

    @PostMapping("/applications/{applicationId}/cancel")
    /**
     * 取消当前用户名下的旅行申请。
     *
     * @param applicationId 申请 ID
     * @param session 当前 HTTP 会话
     * @return 取消成功消息
     */
    public ApiResponse<Void> cancel(@PathVariable Long applicationId, HttpSession session) {
        applicationService.cancel(applicationId, authService.currentUser(session));
        return ApiResponse.message("申请已取消");
    }

    @GetMapping("/applications/{applicationId}/companions")
    /**
     * 查询当前用户某条申请下的随行人员列表。
     *
     * @param applicationId 申请 ID
     * @param session 当前 HTTP 会话
     * @return 随行人员列表
     */
    public ApiResponse<List<Companion>> companions(@PathVariable Long applicationId, HttpSession session) {
        return ApiResponse.ok(applicationService.companions(applicationId, authService.currentUser(session)));
    }

    @PostMapping("/applications/{applicationId}/companions")
    /**
     * 覆盖保存当前用户某条申请下的随行人员。
     *
     * @param applicationId 申请 ID
     * @param requests 随行人员请求列表
     * @param session 当前 HTTP 会话
     * @return 保存后的随行人员列表
     */
    public ApiResponse<List<Companion>> saveCompanions(@PathVariable Long applicationId,
                                                       @Valid @RequestBody List<CompanionRequest> requests,
                                                       HttpSession session) {
        return ApiResponse.ok(applicationService.replaceCompanions(applicationId, authService.currentUser(session), requests));
    }

    @GetMapping("/my-applications")
    /**
     * 查询当前登录用户的全部旅行申请。
     *
     * @param session 当前 HTTP 会话
     * @return 我的申请列表
     */
    public ApiResponse<List<Application>> myApplications(HttpSession session) {
        return ApiResponse.ok(applicationService.myApplications(authService.currentUser(session)));
    }

    @GetMapping("/my-applications/export.pdf")
    /**
     * 将当前用户的申请列表导出为 PDF 文件。
     *
     * @param session 当前 HTTP 会话
     * @return PDF 文件字节响应
     */
    public ResponseEntity<byte[]> export(HttpSession session) {
        User user = authService.currentUser(session);
        byte[] pdf = pdfExportService.exportApplications(user, applicationService.myApplications(user));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("my-applications.pdf").build().toString())
                .body(pdf);
    }
}
