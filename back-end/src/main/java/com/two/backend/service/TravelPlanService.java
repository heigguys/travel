package com.two.backend.service;

import com.two.backend.dto.PlanRequest;
import com.two.backend.mapper.ApplicationMapper;
import com.two.backend.mapper.CompanionMapper;
import com.two.backend.mapper.ConsultationMapper;
import com.two.backend.mapper.TravelPlanMapper;
import com.two.backend.model.Application;
import com.two.backend.model.TravelPlan;
import com.two.backend.model.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
/**
 * 旅行计划服务，封装计划查询、可见性校验、维护和附件读取。
 */
public class TravelPlanService {
    private static final BigDecimal MIN_PLAN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PLAN_PRICE = new BigDecimal("10000");
    private static final int MIN_CAPACITY = 10;
    private static final int MAX_CAPACITY = 50;
    private static final DateTimeFormatter PLAN_NO_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final TravelPlanMapper travelPlanMapper;
    private final ApplicationMapper applicationMapper;
    private final CompanionMapper companionMapper;
    private final ConsultationMapper consultationMapper;
    private final StorageService storageService;
    private final MailNotificationService mailNotificationService;

    public TravelPlanService(TravelPlanMapper travelPlanMapper, ApplicationMapper applicationMapper,
                             CompanionMapper companionMapper, ConsultationMapper consultationMapper,
                             StorageService storageService, MailNotificationService mailNotificationService) {
        this.travelPlanMapper = travelPlanMapper;
        this.applicationMapper = applicationMapper;
        this.companionMapper = companionMapper;
        this.consultationMapper = consultationMapper;
        this.storageService = storageService;
        this.mailNotificationService = mailNotificationService;
    }

    /**
     * 按用户角色和筛选条件查询旅行计划列表。
     *
     * @param user 当前用户
     * @param keyword 目的地或计划编号关键字
     * @param status 计划状态
     * @param published 公开状态
     * @param sort 排序字段
     * @return 当前用户可见的旅行计划列表
     */
    public List<TravelPlan> list(User user, String keyword, Integer status, Boolean published, String sort, String sortDir) {
        return travelPlanMapper.list(Integer.valueOf(User.ROLE_ADMIN).equals(user.getRole()), user.getId(), keyword, status, published, sort, sortDir);
    }

    /**
     * 根据日期计算新计划的初始状态（新计划申请人数为 0）。
     */
    private int computeInitialStatus(LocalDate startDate, LocalDate endDate, Boolean published) {
        if (!Boolean.TRUE.equals(published)) {
            return TravelPlan.STATUS_UNPUBLISHED;
        }
        LocalDate today = LocalDate.now();
        if (endDate.isBefore(today))       return TravelPlan.STATUS_DISBANDED;
        if (!startDate.isAfter(today))     return TravelPlan.STATUS_DISBANDED;
        return TravelPlan.STATUS_AVAILABLE;
    }

    /**
     * 每天凌晨 0 点批量刷新所有旅行计划状态。
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledStatusRefresh() {
        travelPlanMapper.updateAllStatuses();
    }

    /**
     * 查询计划并校验当前用户是否可见。
     *
     * @param id 旅行计划 ID
     * @param user 当前用户
     * @return 可见的旅行计划
     */
    public TravelPlan getVisible(Long id, User user) {
        TravelPlan plan = travelPlanMapper.findById(id);
        if (plan == null) {
            throw new BusinessException("旅行计划不存在");
        }
        if (!Integer.valueOf(User.ROLE_ADMIN).equals(user.getRole()) && !Boolean.TRUE.equals(plan.getPublished())) {
            throw new BusinessException("该旅行计划未公开");
        }
        return plan;
    }

    /**
     * 新增旅行计划并保存可选 PDF 附件。
     *
     * @param request 计划请求
     * @param file PDF 附件
     * @return 新增后的旅行计划
     */
    public TravelPlan create(PlanRequest request, MultipartFile file) throws IOException {
        validatePlan(request);
        validateCreateStartDate(request);
        TravelPlan plan = new TravelPlan();
        plan.setPlanNo(generatePlanNo());
        fillPlan(plan, request, file);
        plan.setStatus(computeInitialStatus(request.startDate(), request.endDate(), plan.getPublished()));
        travelPlanMapper.insert(plan);
        return plan;
    }

    private void validateCreateStartDate(PlanRequest request) {
        if (!request.startDate().isAfter(LocalDate.now())) {
            throw new BusinessException("启程日最早可以选择明天");
        }
    }

    private String generatePlanNo() {
        String prefix = "TP" + LocalDate.now().format(PLAN_NO_DATE_FORMATTER);
        String maxPlanNo = travelPlanMapper.findMaxPlanNoByPrefix(prefix);
        int nextSequence = 1;
        if (maxPlanNo != null && maxPlanNo.length() > prefix.length()) {
            nextSequence = Integer.parseInt(maxPlanNo.substring(prefix.length())) + 1;
        }
        return prefix + String.format("%03d", nextSequence);
    }

    /**
     * 更新已有旅行计划，并在上传新附件时替换附件信息。
     *
     * @param id 旅行计划 ID
     * @param request 计划请求
     * @param file PDF 附件
     * @return 更新后的旅行计划
     */
    public TravelPlan update(Long id, PlanRequest request, MultipartFile file) throws IOException {
        validatePlan(request);
        TravelPlan plan = travelPlanMapper.findById(id);
        if (plan == null) {
            throw new BusinessException("旅行计划不存在");
        }
        if (Boolean.TRUE.equals(plan.getPublished())) {
            throw new BusinessException("已公开的旅行计划不可编辑");
        }
        fillPlan(plan, request, file);
        travelPlanMapper.update(plan);
        travelPlanMapper.updateAllStatuses();
        return plan;
    }

    /**
     * 删除计划前查询该计划下的有效申请，用于前端确认提示。
     *
     * @param id 旅行计划 ID
     * @return 有效申请列表
     */
    public List<Application> deletePreview(Long id) {
        TravelPlan plan = travelPlanMapper.findById(id);
        if (plan == null) {
            throw new BusinessException("旅行计划不存在");
        }
        return applicationMapper.listActiveByPlan(id);
    }

    @Transactional
    /**
     * 删除旅行计划，并先清理该计划下的申请记录。
     *
     * @param id 旅行计划 ID
     */
    public void delete(Long id) {
        List<Application> applicants = deletePreview(id);
        if (!applicants.isEmpty()) {
            throw new BusinessException("已经有员工申请本计划。如需删除本计划，请先发送邮件通知员工。");
        }
        deletePlanCascade(id);
    }

    @Transactional
    /**
     * 向已申请员工发送取消通知邮件后删除旅行计划。
     *
     * @param id 旅行计划 ID
     */
    public void notifyCancelAndDelete(Long id) {
        TravelPlan plan = travelPlanMapper.findById(id);
        if (plan == null) {
            throw new BusinessException("旅行计划不存在");
        }
        List<Application> applicants = applicationMapper.listActiveByPlan(id);
        if (applicants.isEmpty()) {
            deletePlanCascade(id);
            return;
        }
        deletePlanCascade(id);
        try {
            mailNotificationService.sendPlanCancelNotice(plan, applicants);
        } catch (RuntimeException ignored) {
            // 邮件发送失败不阻断计划删除，前端不再要求等待邮件发送结果。
        }
    }

    private void deletePlanCascade(Long id) {
        companionMapper.deleteByPlan(id);
        try {
            consultationMapper.deleteReadsByPlan(id);
            consultationMapper.deleteByPlan(id);
        } catch (RuntimeException ignored) {
            // 咨询辅助数据清理失败不应阻断计划和申请删除。
        }
        applicationMapper.deleteByPlan(id);
        travelPlanMapper.delete(id);
    }

    /**
     * 读取当前用户可访问的旅行计划 PDF 附件。
     *
     * @param id 旅行计划 ID
     * @param user 当前用户
     * @return PDF 附件资源
     */
    public Resource file(Long id, User user) {
        TravelPlan plan = getVisible(id, user);
        if (plan.getFilePath() == null || plan.getFilePath().isBlank()) {
            throw new BusinessException("该计划没有上传 PDF 附件");
        }
        return storageService.load(plan.getFilePath());
    }

    /**
     * 校验旅行计划必填项和日期范围。
     *
     * @param request 计划请求
     */
    private void validatePlan(PlanRequest request) {
        if (request.destination() == null || request.destination().isBlank()
                || request.startDate() == null || request.endDate() == null
                || request.price() == null || request.capacity() == null) {
            throw new BusinessException("请填写所有必填项");
        }
        if (request.destination().length() > 10) {
            throw new BusinessException("目的地不能超过10个字符");
        }
        if (request.price().compareTo(MIN_PLAN_PRICE) <= 0) {
            throw new BusinessException("价格必须大于0");
        }
        if (request.price().compareTo(MAX_PLAN_PRICE) > 0) {
            throw new BusinessException("单人价格上限为 10000 元");
        }
        if (request.capacity() < MIN_CAPACITY) {
            throw new BusinessException("定员数不能少于 10 人");
        }
        if (request.capacity() > MAX_CAPACITY) {
            throw new BusinessException("定员数不能超过 50 人");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("返回日不能早于启程日");
        }
    }

    /**
     * 将请求字段写入实体，并在存在附件时保存附件信息。
     *
     * @param plan 旅行计划实体
     * @param request 计划请求
     * @param file PDF 附件
     */
    private void fillPlan(TravelPlan plan, PlanRequest request, MultipartFile file) throws IOException {
        plan.setDestination(request.destination());
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setPrice(request.price());
        plan.setCapacity(request.capacity());
        plan.setPublished(Boolean.TRUE.equals(request.published()));
        StorageService.StoredFile storedFile = storageService.store(file);
        if (storedFile != null) {
            plan.setFilePath(storedFile.path());
            plan.setFileName(storedFile.originalName());
        }
    }

}
