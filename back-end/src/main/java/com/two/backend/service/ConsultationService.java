package com.two.backend.service;

import com.two.backend.dto.ConsultationRequest;
import com.two.backend.mapper.ConsultationMapper;
import com.two.backend.mapper.TravelPlanMapper;
import com.two.backend.model.Consultation;
import com.two.backend.model.TravelPlan;
import com.two.backend.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
/**
 * 咨询服务，控制计划可见性并维护咨询消息状态。
 */
public class ConsultationService {
    private final ConsultationMapper consultationMapper;
    private final TravelPlanMapper travelPlanMapper;

    public ConsultationService(ConsultationMapper consultationMapper, TravelPlanMapper travelPlanMapper) {
        this.consultationMapper = consultationMapper;
        this.travelPlanMapper = travelPlanMapper;
    }

    /**
     * 查询当前用户可见的计划咨询列表。
     *
     * @param planId 旅行计划 ID
     * @param user 当前用户
     * @return 咨询消息列表
     */
    public List<Consultation> list(Long planId, User user) {
        ensurePlanVisible(planId, user);
        return consultationMapper.listByPlan(planId, user.getId(), Integer.valueOf(User.ROLE_ADMIN).equals(user.getRole()));
    }

    /**
     * 保存一条当前用户发送的咨询消息。
     *
     * @param planId 旅行计划 ID
     * @param user 当前用户
     * @param request 咨询请求
     * @return 新增的咨询消息
     */
    public Consultation send(Long planId, User user, ConsultationRequest request) {
        ensurePlanVisible(planId, user);
        Consultation consultation = new Consultation();
        consultation.setPlanId(planId);
        consultation.setUserId(user.getId());
        consultation.setParticipantUserId(user.getId());
        consultation.setSenderRole(user.getRole());
        consultation.setContent(request.content());
        consultation.setStatus("OPEN");
        consultationMapper.insert(consultation);
        return consultation;
    }

    /**
     * 关闭当前用户在指定计划下的咨询对话。
     *
     * @param planId 旅行计划 ID
     * @param user 当前用户
     */
    public void close(Long planId, User user) {
        ensurePlanVisible(planId, user);
        consultationMapper.close(planId, user.getId());
    }

    /**
     * 确认计划存在且当前用户有权限查看。
     *
     * @param planId 旅行计划 ID
     * @param user 当前用户
     */
    private void ensurePlanVisible(Long planId, User user) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null || (!Integer.valueOf(User.ROLE_ADMIN).equals(user.getRole()) && !Boolean.TRUE.equals(plan.getPublished()))) {
            throw new BusinessException("旅行计划不存在或未公开");
        }
    }
}
