package com.two.backend.service;

import com.two.backend.dto.PlanRequest;
import com.two.backend.mapper.ApplicationMapper;
import com.two.backend.mapper.TravelPlanMapper;
import com.two.backend.model.Application;
import com.two.backend.model.TravelPlan;
import com.two.backend.model.User;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
/**
 * 旅行计划服务，封装计划查询、可见性校验、维护和附件读取。
 */
public class TravelPlanService {
    private final TravelPlanMapper travelPlanMapper;
    private final ApplicationMapper applicationMapper;
    private final StorageService storageService;

    public TravelPlanService(TravelPlanMapper travelPlanMapper, ApplicationMapper applicationMapper, StorageService storageService) {
        this.travelPlanMapper = travelPlanMapper;
        this.applicationMapper = applicationMapper;
        this.storageService = storageService;
    }

    /**
     * 按用户角色和筛选条件查询旅行计划列表。
     *
     * @param user 当前用户
     * @param keyword 目的地或计划编号关键字
     * @param status 计划状态
     * @param sort 排序字段
     * @return 当前用户可见的旅行计划列表
     */
    public List<TravelPlan> list(User user, String keyword, Integer status, String sort) {
        return travelPlanMapper.list(Integer.valueOf(User.ROLE_ADMIN).equals(user.getRole()), user.getId(), keyword, status, sort);
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
        TravelPlan plan = new TravelPlan();
        plan.setPlanNo("TP" + System.currentTimeMillis());
        fillPlan(plan, request, file);
        plan.setStatus(TravelPlan.STATUS_APPLYING);
        travelPlanMapper.insert(plan);
        return plan;
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
        fillPlan(plan, request, file);
        plan.setStatus(statusFor(id, plan.getCapacity()));
        travelPlanMapper.update(plan);
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
                || request.price() == null || request.capacity() == null || request.capacity() < 1) {
            throw new BusinessException("请填写所有必填项");
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

    /**
     * 根据当前有效申请人数和定员计算报名状态。
     *
     * @param planId 旅行计划 ID
     * @param capacity 定员
     * @return 计划状态
     */
    private Integer statusFor(Long planId, Integer capacity) {
        return applicationMapper.activeCount(planId) >= capacity ? TravelPlan.STATUS_FULL : TravelPlan.STATUS_APPLYING;
    }
}
