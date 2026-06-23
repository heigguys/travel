package com.two.backend.controller;

import com.two.backend.dto.ApiResponse;
import com.two.backend.dto.ConsultationRequest;
import com.two.backend.model.Consultation;
import com.two.backend.model.ConsultationSession;
import com.two.backend.service.AuthService;
import com.two.backend.service.ConsultationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans/{planId}/consultations")
/**
 * 咨询控制器，处理用户和管理员围绕旅行计划的咨询消息。
 */
public class ConsultationController {
    private final AuthService authService;
    private final ConsultationService consultationService;

    public ConsultationController(AuthService authService, ConsultationService consultationService) {
        this.authService = authService;
        this.consultationService = consultationService;
    }

    @GetMapping
    /**
     * 查询某个旅行计划下当前用户可见的咨询消息。
     *
     * @param planId 旅行计划 ID
     * @param session 当前 HTTP 会话
     * @return 咨询消息列表
     */
    public ApiResponse<List<Consultation>> list(@PathVariable Long planId,
                                                @RequestParam(required = false) Long participantUserId,
                                                HttpSession session) {
        return ApiResponse.ok(consultationService.list(planId, authService.currentUser(session), participantUserId));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ConsultationSession>> sessions(@PathVariable Long planId, HttpSession session) {
        return ApiResponse.ok(consultationService.listSessions(planId, authService.currentUser(session)));
    }

    @PostMapping
    /**
     * 发送一条旅行计划咨询消息。
     *
     * @param planId 旅行计划 ID
     * @param request 咨询内容
     * @param session 当前 HTTP 会话
     * @return 新建的咨询消息
     */
    public ApiResponse<Consultation> send(@PathVariable Long planId, @Valid @RequestBody ConsultationRequest request, HttpSession session) {
        return ApiResponse.ok(consultationService.send(planId, authService.currentUser(session), request));
    }

    @PostMapping("/close")
    /**
     * 关闭当前用户在指定计划下的咨询对话。
     *
     * @param planId 旅行计划 ID
     * @param session 当前 HTTP 会话
     * @return 关闭成功消息
     */
    public ApiResponse<Void> close(@PathVariable Long planId, HttpSession session) {
        consultationService.close(planId, authService.currentUser(session));
        return ApiResponse.message("对话已结束");
    }
}
