package com.two.backend.service;

import com.two.backend.dto.ConsultationRequest;
import com.two.backend.mapper.ConsultationMapper;
import com.two.backend.mapper.TravelPlanMapper;
import com.two.backend.model.Consultation;
import com.two.backend.model.Role;
import com.two.backend.model.TravelPlan;
import com.two.backend.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultationService {
    private final ConsultationMapper consultationMapper;
    private final TravelPlanMapper travelPlanMapper;

    public ConsultationService(ConsultationMapper consultationMapper, TravelPlanMapper travelPlanMapper) {
        this.consultationMapper = consultationMapper;
        this.travelPlanMapper = travelPlanMapper;
    }

    public List<Consultation> list(Long planId, User user) {
        ensurePlanVisible(planId, user);
        return consultationMapper.listByPlan(planId, user.getId(), user.getRole() == Role.ADMIN);
    }

    public Consultation send(Long planId, User user, ConsultationRequest request) {
        ensurePlanVisible(planId, user);
        Consultation consultation = new Consultation();
        consultation.setPlanId(planId);
        consultation.setUserId(user.getId());
        consultation.setParticipantUserId(user.getId());
        consultation.setSenderRole(user.getRole().name());
        consultation.setContent(request.content());
        consultation.setStatus("OPEN");
        consultationMapper.insert(consultation);
        return consultation;
    }

    public void close(Long planId, User user) {
        ensurePlanVisible(planId, user);
        consultationMapper.close(planId, user.getId());
    }

    private void ensurePlanVisible(Long planId, User user) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null || (user.getRole() != Role.ADMIN && !Boolean.TRUE.equals(plan.getPublished()))) {
            throw new BusinessException("旅行计划不存在或未公开");
        }
    }
}
