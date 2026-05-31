package com.two.backend.service;

import com.two.backend.dto.PlanRequest;
import com.two.backend.mapper.ApplicationMapper;
import com.two.backend.mapper.TravelPlanMapper;
import com.two.backend.model.Application;
import com.two.backend.model.Role;
import com.two.backend.model.TravelPlan;
import com.two.backend.model.User;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TravelPlanService {
    private final TravelPlanMapper travelPlanMapper;
    private final ApplicationMapper applicationMapper;
    private final StorageService storageService;

    public TravelPlanService(TravelPlanMapper travelPlanMapper, ApplicationMapper applicationMapper, StorageService storageService) {
        this.travelPlanMapper = travelPlanMapper;
        this.applicationMapper = applicationMapper;
        this.storageService = storageService;
    }

    public List<TravelPlan> list(User user, String keyword, String status, String sort) {
        return travelPlanMapper.list(user.getRole() == Role.ADMIN, user.getId(), keyword, status, sort);
    }

    public TravelPlan getVisible(Long id, User user) {
        TravelPlan plan = travelPlanMapper.findById(id);
        if (plan == null) {
            throw new BusinessException("旅行计划不存在");
        }
        if (user.getRole() != Role.ADMIN && !Boolean.TRUE.equals(plan.getPublished())) {
            throw new BusinessException("该旅行计划未公开");
        }
        return plan;
    }

    public TravelPlan create(PlanRequest request, MultipartFile file) throws IOException {
        validatePlan(request);
        TravelPlan plan = new TravelPlan();
        plan.setPlanNo("TP" + System.currentTimeMillis());
        fillPlan(plan, request, file);
        plan.setStatus("未开始");
        travelPlanMapper.insert(plan);
        return plan;
    }

    public TravelPlan update(Long id, PlanRequest request, MultipartFile file) throws IOException {
        validatePlan(request);
        TravelPlan plan = travelPlanMapper.findById(id);
        if (plan == null) {
            throw new BusinessException("旅行计划不存在");
        }
        fillPlan(plan, request, file);
        travelPlanMapper.update(plan);
        return plan;
    }

    public List<Application> deletePreview(Long id) {
        TravelPlan plan = travelPlanMapper.findById(id);
        if (plan == null) {
            throw new BusinessException("旅行计划不存在");
        }
        return applicationMapper.listActiveByPlan(id);
    }

    @Transactional
    public void delete(Long id) {
        applicationMapper.deleteByPlan(id);
        travelPlanMapper.delete(id);
    }

    public Resource file(Long id, User user) {
        TravelPlan plan = getVisible(id, user);
        if (plan.getFilePath() == null || plan.getFilePath().isBlank()) {
            throw new BusinessException("该计划没有上传 PDF 附件");
        }
        return storageService.load(plan.getFilePath());
    }

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

    private void fillPlan(TravelPlan plan, PlanRequest request, MultipartFile file) throws IOException {
        plan.setDestination(request.destination());
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setPrice(request.price());
        plan.setCapacity(request.capacity());
        plan.setPublished(Boolean.TRUE.equals(request.published()));
        plan.setStatus(statusFor(request));
        StorageService.StoredFile storedFile = storageService.store(file);
        if (storedFile != null) {
            plan.setFilePath(storedFile.path());
            plan.setFileName(storedFile.originalName());
        }
    }

    private String statusFor(PlanRequest request) {
        return "未开始";
    }
}
