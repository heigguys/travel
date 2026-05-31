package com.two.backend.service;

import com.two.backend.dto.ApplicationRequest;
import com.two.backend.dto.CompanionRequest;
import com.two.backend.mapper.ApplicationMapper;
import com.two.backend.mapper.CompanionMapper;
import com.two.backend.mapper.TravelPlanMapper;
import com.two.backend.model.Application;
import com.two.backend.model.Companion;
import com.two.backend.model.TravelPlan;
import com.two.backend.model.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
    private final ApplicationMapper applicationMapper;
    private final CompanionMapper companionMapper;
    private final TravelPlanMapper travelPlanMapper;

    public ApplicationService(ApplicationMapper applicationMapper, CompanionMapper companionMapper, TravelPlanMapper travelPlanMapper) {
        this.applicationMapper = applicationMapper;
        this.companionMapper = companionMapper;
        this.travelPlanMapper = travelPlanMapper;
    }

    @Transactional
    public Application apply(Long planId, User user, ApplicationRequest request) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null || !Boolean.TRUE.equals(plan.getPublished())) {
            throw new BusinessException("旅行计划不存在或未公开");
        }
        Application active = applicationMapper.findActive(planId, user.getId());
        int current = applicationMapper.activeCount(planId);
        int oldCount = active == null ? 0 : active.getApplicantCount();
        if (current - oldCount + request.applicantCount() > plan.getCapacity()) {
            throw new BusinessException("申请人数超过定员");
        }
        if (active == null) {
            active = new Application();
            active.setPlanId(planId);
            active.setUserId(user.getId());
            active.setStatus("ACTIVE");
            active.setApplicantCount(request.applicantCount());
            active.setOptionText(request.optionText());
            applicationMapper.insert(active);
        } else {
            active.setApplicantCount(request.applicantCount());
            active.setOptionText(request.optionText());
            applicationMapper.update(active);
        }
        return active;
    }

    public List<Application> myApplications(User user) {
        return applicationMapper.listByUser(user.getId());
    }

    public void cancel(Long applicationId, User user) {
        Application application = ownedApplication(applicationId, user);
        applicationMapper.cancel(application.getId());
    }

    public List<Companion> companions(Long applicationId, User user) {
        ownedApplication(applicationId, user);
        return companionMapper.listByApplication(applicationId);
    }

    @Transactional
    public List<Companion> replaceCompanions(Long applicationId, User user, List<CompanionRequest> requests) {
        Application application = ownedApplication(applicationId, user);
        if (requests.size() > application.getApplicantCount()) {
            throw new BusinessException("随行人员数量不能超过申请人数");
        }
        companionMapper.deleteByApplication(applicationId);
        for (CompanionRequest request : requests) {
            Companion companion = new Companion();
            companion.setApplicationId(applicationId);
            companion.setName(request.name());
            companion.setGender(request.gender());
            companion.setIdCard(request.idCard());
            companion.setBedNeeded(Boolean.TRUE.equals(request.bedNeeded()));
            companionMapper.insert(companion);
        }
        return companionMapper.listByApplication(applicationId);
    }

    private Application ownedApplication(Long applicationId, User user) {
        Application application = applicationMapper.findById(applicationId);
        if (application == null || !application.getUserId().equals(user.getId())) {
            throw new BusinessException("申请记录不存在或无权访问");
        }
        return application;
    }
}
