package com.two.backend.service;

import com.two.backend.dto.ConsultationRequest;
import com.two.backend.mapper.ConsultationMapper;
import com.two.backend.mapper.TravelPlanMapper;
import com.two.backend.model.Consultation;
import com.two.backend.model.ConsultationSession;
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
    public List<Consultation> list(Long planId, User user, Long participantUserId) {
        ensurePlanVisible(planId, user);
        Long visibleParticipantUserId = resolveParticipantUserId(planId, user, participantUserId);
        List<Consultation> consultations = consultationMapper.listByPlanAndParticipant(planId, visibleParticipantUserId);
        if (isAdmin(user)) {
            consultationMapper.markAdminSessionRead(planId, visibleParticipantUserId);
        } else {
            consultationMapper.markUserRead(planId, user.getId());
        }
        return consultations;
    }

    public List<ConsultationSession> listSessions(Long planId, User user) {
        ensurePlanVisible(planId, user);
        if (isAdmin(user)) {
            return consultationMapper.listSessions(planId);
        }
        ConsultationSession session = new ConsultationSession();
        session.setParticipantUserId(user.getId());
        session.setEmployeeNo(user.getEmployeeNo());
        session.setUserName(user.getName());
        session.setUnreadCount(0);
        return List.of(session);
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
        Long visibleParticipantUserId = resolveParticipantUserId(planId, user, request.participantUserId());
        Consultation consultation = new Consultation();
        consultation.setPlanId(planId);
        consultation.setUserId(user.getId());
        consultation.setParticipantUserId(visibleParticipantUserId);
        consultation.setSenderRole(user.getRole());
        consultation.setContent(request.content());
        consultation.setStatus("OPEN");
        consultationMapper.insert(consultation);
        if (isAdmin(user)) {
            consultationMapper.markAdminSessionRead(planId, visibleParticipantUserId);
        } else {
            consultationMapper.markUserRead(planId, user.getId());
        }
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
        if (plan == null || (!isAdmin(user) && !Boolean.TRUE.equals(plan.getPublished()))) {
            throw new BusinessException("旅行计划不存在或未公开");
        }
    }

    private boolean isAdmin(User user) {
        return Integer.valueOf(User.ROLE_ADMIN).equals(user.getRole());
    }

    private Long resolveParticipantUserId(Long planId, User user, Long participantUserId) {
        if (!isAdmin(user)) {
            return user.getId();
        }
        if (participantUserId == null) {
            throw new BusinessException("请选择咨询员工");
        }
        if (consultationMapper.countSession(planId, participantUserId) == 0) {
            throw new BusinessException("请选择已有咨询员工");
        }
        return participantUserId;
    }
}
