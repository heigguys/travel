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
/**
 * 旅行计划控制器，负责计划查询、维护、删除预览和 PDF 附件访问。
 */
public class TravelPlanController {
    private final AuthService authService;
    private final TravelPlanService travelPlanService;

    public TravelPlanController(AuthService authService, TravelPlanService travelPlanService) {
        this.authService = authService;
        this.travelPlanService = travelPlanService;
    }

    @GetMapping
    /**
     * 按关键字、状态和排序条件查询当前用户可见的旅行计划。
     *
     * @param keyword 目的地或计划编号关键字
     * @param status 计划状态
     * @param sort 排序字段
     * @param session 当前 HTTP 会话
     * @return 旅行计划列表
     */
    public ApiResponse<List<TravelPlan>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String sort,
                                              @RequestParam(required = false) String sortDir,
                                              HttpSession session) {
        return ApiResponse.ok(travelPlanService.list(authService.currentUser(session), keyword, status, sort, sortDir));
    }

    @GetMapping("/{id}")
    /**
     * 查询单个旅行计划详情，并按当前用户角色控制可见性。
     *
     * @param id 旅行计划 ID
     * @param session 当前 HTTP 会话
     * @return 旅行计划详情
     */
    public ApiResponse<TravelPlan> detail(@PathVariable Long id, HttpSession session) {
        return ApiResponse.ok(travelPlanService.getVisible(id, authService.currentUser(session)));
    }

    @PostMapping
    /**
     * 管理员新增旅行计划，可同时上传 PDF 附件。
     *
     * @param form 表单字段和附件
     * @param session 当前 HTTP 会话
     * @return 新增后的旅行计划
     */
    public ApiResponse<TravelPlan> create(PlanForm form, HttpSession session) throws IOException {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        return ApiResponse.ok(travelPlanService.create(form.request(), form.file()));
    }

    @PostMapping("/{id}")
    /**
     * 管理员编辑旅行计划，可选择替换 PDF 附件。
     *
     * @param id 旅行计划 ID
     * @param form 表单字段和附件
     * @param session 当前 HTTP 会话
     * @return 更新后的旅行计划
     */
    public ApiResponse<TravelPlan> update(@PathVariable Long id, PlanForm form, HttpSession session) throws IOException {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        return ApiResponse.ok(travelPlanService.update(id, form.request(), form.file()));
    }

    @GetMapping("/{id}/delete-preview")
    /**
     * 删除旅行计划前查询已有申请人，供前端展示确认信息。
     *
     * @param id 旅行计划 ID
     * @param session 当前 HTTP 会话
     * @return 有效申请列表
     */
    public ApiResponse<List<Application>> deletePreview(@PathVariable Long id, HttpSession session) {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        return ApiResponse.ok(travelPlanService.deletePreview(id));
    }

    @PostMapping("/{id}/delete")
    /**
     * 管理员删除旅行计划，并清理关联申请。
     *
     * @param id 旅行计划 ID
     * @param session 当前 HTTP 会话
     * @return 删除成功消息
     */
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        travelPlanService.delete(id);
        return ApiResponse.message("旅行计划已删除");
    }

    @PostMapping("/{id}/notify-cancel-and-delete")
    /**
     * 管理员删除已有申请的旅行计划前，先向申请员工发送取消通知邮件。
     *
     * @param id 旅行计划 ID
     * @param session 当前 HTTP 会话
     * @return 通知并删除成功消息
     */
    public ApiResponse<Void> notifyCancelAndDelete(@PathVariable Long id, HttpSession session) {
        User user = authService.currentUser(session);
        authService.requireAdmin(user);
        travelPlanService.notifyCancelAndDelete(id);
        return ApiResponse.message("邮件通知已发送，旅行计划已删除");
    }

    @GetMapping("/{id}/file")
    /**
     * 读取旅行计划的 PDF 附件并以内联方式返回。
     *
     * @param id 旅行计划 ID
     * @param session 当前 HTTP 会话
     * @return PDF 附件资源响应
     */
    public ResponseEntity<Resource> file(@PathVariable Long id, HttpSession session) {
        Resource resource = travelPlanService.file(id, authService.currentUser(session));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(resource.getFilename()).build().toString())
                .body(resource);
    }

    /**
     * 接收 multipart/form-data 表单，并转换为服务层使用的 PlanRequest。
     */
    public record PlanForm(
            String destination,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            BigDecimal price,
            Integer capacity,
            Boolean published,
            MultipartFile file
    ) {
        /**
         * 将表单字段转换为旅行计划请求对象，附件由调用方单独处理。
         *
         * @return 旅行计划请求对象
         */
        PlanRequest request() {
            return new PlanRequest(destination, startDate, endDate, price, capacity, published);
        }
    }
}
